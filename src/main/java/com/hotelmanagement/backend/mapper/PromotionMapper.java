package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.PromotionCreationRequest;
import com.hotelmanagement.backend.dto.request.PromotionUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.entity.Promotion;
import com.hotelmanagement.backend.entity.Room;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active",  ignore = true)
    Promotion toPromotion(PromotionCreationRequest request);

    PromotionResponse toPromotionResponse(Promotion promotion);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePromotion(@MappingTarget Promotion promotion, PromotionUpdateRequest request);
}
