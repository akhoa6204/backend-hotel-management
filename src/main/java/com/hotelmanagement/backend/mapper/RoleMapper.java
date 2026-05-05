package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.request.CreateRoleRequest;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.dto.response.RoleResponse;
import com.hotelmanagement.backend.entity.Permisson;
import com.hotelmanagement.backend.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(CreateRoleRequest request);
    RoleResponse toRoleResponse(Role role);
}
