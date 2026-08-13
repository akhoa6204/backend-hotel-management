package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeImageRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.entity.*;
import com.hotelmanagement.backend.enums.BookingStatus;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomTypeService {
    RoomTypeRepository roomTypeRepository;
    RoomTypeMapper roomTypeMapper;

    AmenityRepository amenityRepository;
    PromotionService promotionService;
    RoomService roomService;
    CloudinaryService cloudinaryService;

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

        roomType.setRoomTypeImages(buildRoomTypeImages(
                roomType,
                request.getRoomTypeImageMetadata(),
                request.getRoomTypeImages()
        ));

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
        List<String> publicIdsToDelete = getImagePublicIds(roomType);
        roomType.setActive(false);
        roomType.getRoomTypeImages().clear();

        roomTypeRepository.save(roomType);
        cleanupCloudinaryImages(publicIdsToDelete);
    }

    public RoomTypeResponse updateRoomType(Long id, RoomTypeUpdateRequest request){
        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        List<String> previousPublicIds = getImagePublicIds(roomType);

        roomTypeMapper.updateRoomType(roomType, request);

        Set<Amenity> amenities = new HashSet<>(
                amenityRepository.findAllById(request.getAmenities())
        );
        roomType.setAmenities(amenities);

        Set<RoomTypeImage> nextImages = buildRoomTypeImages(
                roomType,
                request.getRoomTypeImageMetadata(),
                request.getRoomTypeImages()
        );

        Set<String> retainedPublicIds = nextImages.stream()
                .map(RoomTypeImage::getPublicId)
                .filter(publicId -> publicId != null && !publicId.isBlank())
                .collect(Collectors.toSet());

        roomType.getRoomTypeImages().clear();
        roomType.getRoomTypeImages().addAll(nextImages);

        RoomTypeResponse response = roomTypeMapper.toRoomTypeResponse(roomTypeRepository.save(roomType));

        cleanupCloudinaryImages(
                previousPublicIds.stream()
                        .filter(publicId -> !retainedPublicIds.contains(publicId))
                        .toList()
        );

        return response;
    }

    private Set<RoomTypeImage> buildRoomTypeImages(
            RoomType roomType,
            List<RoomTypeImageRequest> imageMetadata,
            Set<String> imageUrls
    ) {
        Set<RoomTypeImage> images = new HashSet<>();
        int index = 0;

        if (imageMetadata != null && !imageMetadata.isEmpty()) {
            for (RoomTypeImageRequest imageRequest : imageMetadata) {
                cloudinaryService.validateImageMetadata(
                        imageRequest.getSecureUrl(),
                        imageRequest.getPublicId(),
                        "room-types"
                );
                RoomTypeImage image = RoomTypeImage.builder()
                        .url(imageRequest.getSecureUrl())
                        .publicId(imageRequest.getPublicId())
                        .alt(imageRequest.getAlt())
                        .thumbnail(index == 0)
                        .roomType(roomType)
                        .build();
                images.add(image);
                index++;
            }

            return images;
        }

        if (imageUrls == null) {
            return images;
        }

        for (String url : imageUrls) {
            RoomTypeImage image = RoomTypeImage.builder()
                    .url(url)
                    .thumbnail(index == 0)
                    .roomType(roomType)
                    .build();
            images.add(image);
            index++;
        }

        return images;
    }

    private List<String> getImagePublicIds(RoomType roomType) {
        if (roomType.getRoomTypeImages() == null) {
            return List.of();
        }

        return roomType.getRoomTypeImages().stream()
                .map(RoomTypeImage::getPublicId)
                .filter(publicId -> publicId != null && !publicId.isBlank())
                .toList();
    }

    private void cleanupCloudinaryImages(List<String> publicIds) {
        for (String publicId : publicIds) {
            cloudinaryService.deleteImageQuietly(publicId);
        }
    }

    public Page<RoomTypeResponse> getPublicRoomTypes(
            PageRequest pageRequest,
            String q,
            LocalDate startDate,
            LocalDate endDate,
            Integer capacity
    ) {
        boolean hasAvailabilityFilter =
                startDate != null && endDate != null && capacity != null;

        Page<RoomType> roomTypes = roomTypeRepository
                .findByNameContainingIgnoreCaseAndActiveTrue(q, pageRequest);

        LocalDate now = LocalDate.now();
        return roomTypes.map(roomType -> {
            RoomTypeResponse response =
                    roomTypeMapper.toRoomTypeResponse(roomType);

            BigDecimal basePrice = roomType.getBasePrice();

            BigDecimal discountAmount = promotionService.calculateAutoDiscountAmount(
                    basePrice,
                    now,
                    now
            );

            response.setDiscountAmount(discountAmount);

            if (hasAvailabilityFilter) {
                Optional<Long> availableRoomId = roomService.findFirstAvailableRoomIdByRoomType(
                        roomType.getId(),
                        startDate,
                        endDate,
                        capacity
                );

                response.setRoomId(availableRoomId.orElse(null));

                response.setIsAvailable(availableRoomId.isPresent());
            }

            return response;
        });
    }

    public RoomTypeResponse getPublicRoomType(
            Long id
    ) {
        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));
        LocalDate now = LocalDate.now();
        RoomTypeResponse response = roomTypeMapper.toRoomTypeResponse(roomType);

        BigDecimal basePrice = roomType.getBasePrice();

        BigDecimal discountAmount = promotionService.calculateAutoDiscountAmount(
                basePrice,
                now,
                now
        );

        response.setDiscountAmount(discountAmount);

        return response;
    }

}
