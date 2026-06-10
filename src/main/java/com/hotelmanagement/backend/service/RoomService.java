package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.RoomType;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.RoomStatus;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.RoomMapper;
import com.hotelmanagement.backend.repository.RoomRepository;
import com.hotelmanagement.backend.repository.RoomTypeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {
    RoomRepository roomRepository;

    RoomTypeService roomTypeService;
    RoomTypeRepository roomTypeRepository;

    RoomMapper roomMapper;

    public Room createRoom(RoomCreationRequest request) {

        if (roomRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new AppException(ErrorCode.ROOM_ALREADY_EXISTS);
        }

        Room room = roomMapper.toRoom(request);

        room.setActive(true);
        room.setStatus(RoomStatus.VACANT_CLEAN);

        Long roomTypeId = request.getRoomTypeId();

        RoomType roomType = roomTypeRepository.findByIdAndActiveTrue(roomTypeId)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_TYPE_NOT_FOUND));

        room.setRoomType(roomType);

        return roomRepository.save(room);
    }

    public Page<Room> getList(
            PageRequest request,
            String q,
            LocalDate startDate,
            LocalDate endDate,
            Long roomTypeId){
        return roomRepository.getRoomsWithParams(q, roomTypeId, startDate, endDate, request);
    }

    public Room getByid (Long id){
        Room room = roomRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        return room;
    }

    public void deleteById(Long id){
        Room room = roomRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        room.setActive(false);
        roomRepository.save(room);
    }

    public Room updateRoom(Long id, RoomUpdateRequest request) {
        Room room = roomRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));

        if (!Objects.equals(room.getName(), request.getName()) &&
                roomRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new AppException(ErrorCode.ROOM_ALREADY_EXISTS);
        }

        roomMapper.updateRoom(room, request);

        if(request.getRoomTypeId() != null){
            RoomType roomType = roomTypeService.getRoomType(request.getRoomTypeId());
            room.setRoomType(roomType);
        }

        return roomRepository.save(room);

    }

    public Room findRoomAvailable(Long id, LocalDate startDate, LocalDate endDate){
        return roomRepository.roomAvailable(id, startDate, endDate)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_ALREADY_EXISTS));
    }


}
