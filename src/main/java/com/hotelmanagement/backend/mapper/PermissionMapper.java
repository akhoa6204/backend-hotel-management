package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.entity.Permisson;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permisson toPermission(CreatePermissionRequest request);
    PermissionResponse toPermissionResponse(Permisson permission);
}
