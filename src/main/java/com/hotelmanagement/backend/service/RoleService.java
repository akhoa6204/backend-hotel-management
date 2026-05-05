package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.request.CreateRoleRequest;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.dto.response.RoleResponse;
import com.hotelmanagement.backend.entity.Permisson;
import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.PermissionMapper;
import com.hotelmanagement.backend.mapper.RoleMapper;
import com.hotelmanagement.backend.repository.PermissionRepository;
import com.hotelmanagement.backend.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;


    public RoleResponse create(CreateRoleRequest request){
        var role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<Permisson>(permissions));

        role = roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    public Page<RoleResponse> getAll(PageRequest pageRequest){
        return roleRepository.findAll(pageRequest)
                .map(roleMapper::toRoleResponse);

    }

    public void deleteById(String id){
        if(!roleRepository.existsById(id)){
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        roleRepository.deleteById(id);
    }
}
