package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.response.AmenityResponse;
import com.hotelmanagement.backend.entity.Amenity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AmenityMapper {
    AmenityResponse toAmenityResponse(Amenity amenity);
}
