package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.entity.RoomType;
import com.hotelmanagement.backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RoomTypeMapper {
    @Mapping(target = "roomTypeImages", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active",  ignore = true)
    RoomType toRoomType(RoomTypeCreationRequest request);

    RoomTypeResponse toRoomTypeResponse(RoomType roomType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roomTypeImages", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "rooms", ignore = true)
    void updateRoomType(@MappingTarget RoomType roomType, RoomTypeUpdateRequest request);
}
