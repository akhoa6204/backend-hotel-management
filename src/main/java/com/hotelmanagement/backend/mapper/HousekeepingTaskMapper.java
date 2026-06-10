package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.HousekeepingTaskCreationRequest;
import com.hotelmanagement.backend.dto.request.HousekeepingTaskUpdateRequest;
import com.hotelmanagement.backend.dto.response.HousekeepingTaskResponse;
import com.hotelmanagement.backend.entity.HousekeepingTask;
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

}
