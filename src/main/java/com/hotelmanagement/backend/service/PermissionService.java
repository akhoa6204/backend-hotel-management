package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.entity.Permisson;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.PermissionMapper;
import com.hotelmanagement.backend.repository.PermissionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.Permission;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    public PermissionResponse create(CreatePermissionRequest request){
        Permisson permission = permissionMapper.toPermission(request);

        permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    public Page<PermissionResponse> getAll(PageRequest pageRequest){
        return permissionRepository.findAll(pageRequest).map(permissionMapper::toPermissionResponse);
    }

    public void deleteById(String id){
        if(!permissionRepository.existsById(id)){
            throw new AppException(ErrorCode.PERMISSION_NOT_FOUND);
        }
        permissionRepository.deleteById(id);
    }
}
