package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.request.CreateRoleRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.dto.response.RoleResponse;
import com.hotelmanagement.backend.service.PermissionService;
import com.hotelmanagement.backend.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    RoleService roleService;
    @PostMapping("")
    public ApiResponse<RoleResponse> create(@RequestBody CreateRoleRequest request){
        RoleResponse role = roleService.create(request);

        return ApiResponse.<RoleResponse>builder()
                .data(RoleResponse.builder()
                        .name(role.getName())
                        .description(role.getDescription())
                        .permissions(role.getPermissions())
                        .build()).build();
    }

    @GetMapping("")
    public ApiResponse<List<RoleResponse>> getPermissions(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit){
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<RoleResponse> result = roleService.getAll(pageRequest);

        MetaPagination meta = MetaPagination.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrev(result.hasPrevious())
                .build();

        return ApiResponse.<List<RoleResponse>>builder()
                .data(result.getContent())
                .pagination(meta)
                .build();

    }

    @DeleteMapping("/{roleId}")
    public ApiResponse<String> deleteById(@PathVariable String roleId){
        roleService.deleteById(roleId);
        return ApiResponse.<String>builder()
                .message("Role deleted successfully").build();
    }
}
