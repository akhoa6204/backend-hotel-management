package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.EmployeeCreationRequest;
import com.hotelmanagement.backend.dto.request.EmployeeResetPasswordRequest;
import com.hotelmanagement.backend.dto.request.EmployeeUpdateRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {
    UserService userService;
    UserMapper userMapper;

    @GetMapping("")
    ApiResponse<List<UserShortResponse>> getUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false) UserRole role
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<UserShortResponse> result = userService.getEmployees(pageRequest, role).map(userMapper::toUserShortResponse);

        MetaPagination meta = MetaPagination.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrev(result.hasPrevious())
                .build();

        return ApiResponse.<List<UserShortResponse>>builder()
                .data(result.getContent())
                .pagination(meta)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<UserShortResponse> getUser(@PathVariable String id) {
        UserShortResponse userResponse = userMapper.toUserShortResponse(userService.getById(id));

        return ApiResponse.<UserShortResponse>builder().data(userResponse).build();
    }

    @PostMapping("")
    ApiResponse<UserShortResponse> createEmployee(@RequestBody @Valid EmployeeCreationRequest request) {
        UserShortResponse userResponse = userMapper.toUserShortResponse(userService.createEmployee(request));

        return ApiResponse.<UserShortResponse>builder().data(userResponse).build();
    }

    @PatchMapping("/{id}/password")
    ApiResponse<String> resetPassword(@PathVariable String id, @RequestBody @Valid EmployeeResetPasswordRequest request) {
        userService.resetPassword(id, request);

        return ApiResponse.<String>builder().message("Cập nhật mật khẩu thành công").build();
    }

    @PatchMapping("/{id}")
    ApiResponse<UserResponse> updateUser(@PathVariable String id, @RequestBody EmployeeUpdateRequest request){
        UserResponse user =  userMapper.toUserResponse(userService.updateEmployee(id, request));
        return ApiResponse.<UserResponse>builder()
                .data(user)
                .build();
    }
}
