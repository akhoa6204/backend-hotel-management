package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.response.AmenityResponse;
import com.hotelmanagement.backend.dto.response.ShiftResponse;
import com.hotelmanagement.backend.entity.Amenity;
import com.hotelmanagement.backend.entity.Shift;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    ShiftResponse toShiftResponse(Shift shift);
}
