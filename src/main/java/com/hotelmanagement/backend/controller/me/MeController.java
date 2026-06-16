package com.hotelmanagement.backend.controller.me;

import com.hotelmanagement.backend.dto.request.UserUpdatePasswordRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeController {
    UserService userService;
    UserMapper userMapper;

    @GetMapping("")
    ApiResponse<UserShortResponse> getUser() {
        var context = SecurityContextHolder.getContext().getAuthentication();
        String userId = context.getName();

        UserShortResponse userResponse = userMapper.toUserShortResponse(userService.getById(userId));

        return ApiResponse.<UserShortResponse>builder().data(userResponse).build();
    }

    @PutMapping("")
    ApiResponse<UserShortResponse> updateProfile(@RequestBody @Valid UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext().getAuthentication();
        String userId = context.getName();

        UserShortResponse user = userMapper.toUserShortResponse(userService.updateMe(userId, request));
        return ApiResponse.<UserShortResponse>builder()
                .data(user)
                .build();
    }

    @PatchMapping("/password")
    ApiResponse<String> changePassword(@RequestBody @Valid UserUpdatePasswordRequest request) {
        var context = SecurityContextHolder.getContext().getAuthentication();
        String userId = context.getName();

        userService.changeMyPassword(userId, request);
        return ApiResponse.<String>builder()
                .message("Cập nhật mật khẩu thành công")
                .build();
    }
}
