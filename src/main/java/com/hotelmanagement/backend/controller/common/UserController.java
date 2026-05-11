package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.request.UserCreationRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {
    UserService userService;
    UserMapper userMapper;

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<List<UserResponse>> getUsers(
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int limit
    ) {
        SecurityContext context = SecurityContextHolder.getContext();


        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<UserResponse> result =  userService.getUsers(pageRequest).map(userMapper::toUserResponse);

        MetaPagination meta = MetaPagination.builder()
                .page(page)
                .limit(limit)
                .total(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .hasNext(result.hasNext())
                .hasPrev(result.hasPrevious())
                .build();

        return ApiResponse.<List<UserResponse>>builder()
                .data(result.getContent())
                .pagination(meta)
                .build();
    }

    @GetMapping("/me")
    ApiResponse<UserResponse> getUser() {
        var context = SecurityContextHolder.getContext().getAuthentication();
        String userId = context.getName();

        UserResponse userResponse = userMapper.toUserResponse(userService.getById(userId));

        return ApiResponse.<UserResponse>builder().data(userResponse).build();
    }

    @PatchMapping("")
    ApiResponse<UserResponse> updateUser(@RequestBody @Valid UserUpdateRequest request){
        var context = SecurityContextHolder.getContext().getAuthentication();
        String userId = context.getName();

        UserResponse user =  userMapper.toUserResponse(userService.updateUser(userId, request));
        return ApiResponse.<UserResponse>builder()
                .data(user)
                .build();
    }

    @DeleteMapping("/{userId}")
    String deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return "User has been deleted";
    }
}
