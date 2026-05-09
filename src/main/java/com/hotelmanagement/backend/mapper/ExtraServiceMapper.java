package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.ExtraServiceCreationRequest;
import com.hotelmanagement.backend.dto.request.ExtraServiceUpdateRequest;
import com.hotelmanagement.backend.dto.response.ExtraServiceResponse;
import com.hotelmanagement.backend.entity.ExtraService;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExtraServiceMapper {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active",  ignore = true)
    ExtraService toExtraService(ExtraServiceCreationRequest request);

    ExtraServiceResponse toExtraServiceResponse(ExtraService service);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateService(@MappingTarget ExtraService service, ExtraServiceUpdateRequest request);
}
