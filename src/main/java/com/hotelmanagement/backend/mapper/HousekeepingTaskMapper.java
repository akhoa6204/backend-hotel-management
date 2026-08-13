package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.HousekeepingTaskCreationRequest;
import com.hotelmanagement.backend.dto.request.HousekeepingTaskUpdateRequest;
import com.hotelmanagement.backend.dto.response.HousekeepingTaskResponse;
import com.hotelmanagement.backend.dto.response.RoomShortResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.entity.HousekeepingTask;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface HousekeepingTaskMapper {

    HousekeepingTask toHousekeepingTask(HousekeepingTaskCreationRequest housekeepingTaskCreationRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateHousekeepingTask(
            HousekeepingTaskUpdateRequest request,
            @MappingTarget HousekeepingTask housekeepingTask);

    HousekeepingTaskResponse toHousekeepingTaskResponse(HousekeepingTask housekeepingTask);

    default RoomShortResponse toRoomShortResponse(Room room) {
        if (room == null) {
            return null;
        }

        return RoomShortResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .build();
    }

    default UserShortResponse toUserShortResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserShortResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .active(user.isActive())
                .build();
    }

}
