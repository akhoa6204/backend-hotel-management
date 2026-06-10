package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.entity.*;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.RoomTypeMapper;
import com.hotelmanagement.backend.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomTypeService {
    RoomTypeRepository roomTypeRepository;
    RoomTypeMapper roomTypeMapper;

    AmenityRepository amenityRepository;
    public RoomTypeResponse createRoomType(RoomTypeCreationRequest request) {
        if (roomTypeRepository.existsByNameAndActiveTrue(request.getName())){
            throw new AppException(ErrorCode.ROOM_TYPE_ALREADY_EXISTS);
        };

        RoomType roomType = roomTypeMapper.toRoomType(request);

        roomType.setActive(true);

        Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(request.getAmenities()));
        if (amenities.size() != request.getAmenities().size()) {
            throw new AppException(ErrorCode.AMENITY_NOT_FOUND);
        }
        roomType.setAmenities(amenities);

        Set<RoomTypeImage> images = new HashSet<>();
        int index = 0;
        for (String url : request.getRoomTypeImages()) {
            RoomTypeImage image = RoomTypeImage.builder()
                    .url(url)
                    .thumbnail(index == 0)
                    .roomType(roomType)
                    .build();
            images.add(image);
            index++;
        }

        roomType.setRoomTypeImages(images);

        return roomTypeMapper.toRoomTypeResponse(roomTypeRepository.save(roomType));
    }
    public Page<RoomTypeResponse> getRoomTypes(PageRequest request, String q) {
        return roomTypeRepository.findByNameContainingIgnoreCaseAndActiveTrue(q, request).map(roomTypeMapper::toRoomTypeResponse);
    }
    public RoomType getRoomType(Long id) {
        return roomTypeRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
    }
    public void deleteRoomType(Long id){
        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        roomType.setActive(false);

        roomTypeRepository.save(roomType);
    }

    public RoomTypeResponse updateRoomType(Long id, RoomTypeUpdateRequest request){
        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        roomTypeMapper.updateRoomType(roomType, request);

        Set<Amenity> amenities = new HashSet<>(
                amenityRepository.findAllById(request.getAmenities())
        );
        roomType.setAmenities(amenities);

        roomType.getRoomTypeImages().clear();
        int index = 0;
        for (String url : request.getRoomTypeImages()) {
            RoomTypeImage image = RoomTypeImage.builder()
                    .url(url)
                    .thumbnail(index == 0)
                    .roomType(roomType)
                    .build();

            roomType.getRoomTypeImages().add(image);

            index++;
        }

        return roomTypeMapper.toRoomTypeResponse(roomTypeRepository.save(roomType));
    }



}
