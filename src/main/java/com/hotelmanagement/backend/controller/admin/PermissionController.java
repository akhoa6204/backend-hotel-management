package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.CreatePermissionRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.PermissionResponse;
import com.hotelmanagement.backend.repository.PermissionRepository;
import com.hotelmanagement.backend.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Permission;
import java.util.List;

@RestController
@RequestMapping("/permissions")
@PreAuthorize("hasRole('ADMIN')")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PermissionController {
    PermissionService permissionService;
    @PostMapping("")
    public ApiResponse<PermissionResponse> create(@RequestBody CreatePermissionRequest request){
        PermissionResponse permission = permissionService.create(request);

        return ApiResponse.<PermissionResponse>builder()
                .data(PermissionResponse.builder()
                        .name(permission.getName())
                        .description(permission.getDescription())
                        .build()).build();
    }

    @GetMapping("")
    public ApiResponse<List<PermissionResponse>> getPermissions(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit){
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<PermissionResponse> result = permissionService.getAll(pageRequest);

        MetaPagination meta = MetaPagination.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrev(result.hasPrevious())
                .build();

        return ApiResponse.<List<PermissionResponse>>builder()
                .data(result.getContent())
                .pagination(meta)
                .build();

    }

    @DeleteMapping("/{permissionId}")
    public ApiResponse<String> deleteById(@PathVariable String permissionId){
        permissionService.deleteById(permissionId);
        return ApiResponse.<String>builder()
                .message("Permission deleted successfully").build();
    }
}
