package com.hotelmanagement.backend.controller.auth;

import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.AuthenticationResponse;
import com.hotelmanagement.backend.dto.response.IntrospectResponse;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.service.AuthenticationService;
import com.hotelmanagement.backend.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;
    UserService userService;
    UserMapper userMapper;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .data(AuthenticationResponse.builder()
                        .authenticated(result.isAuthenticated())
                        .token(result.getToken())
                        .user(result.getUser())
                        .build())
                .build();
    }

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {

        UserResponse userResponse = userMapper.toUserResponse(userService.createUser(request));

        return ApiResponse.<UserResponse>builder().data(userResponse).build();

    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request) {
        IntrospectResponse result = authenticationService.introspect(request);

        return ApiResponse.<IntrospectResponse>builder()
                .data(IntrospectResponse.builder()
                        .valid(result.isValid())
                        .build())
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<String> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<String>builder()
                .message("Successfully logged out")
                .build();

    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> refresh(@RequestBody RefreshRequest request) throws ParseException, JOSEException {
        AuthenticationResponse response = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .data(response)
                .message("Successfully refreshed token")
                .build();
    }

    @PostMapping("/password-reset-requests")
    ApiResponse<String> requestPasswordReset(@RequestBody @Valid PasswordResetRequest request) {
        authenticationService.requestPasswordReset(request);
        return ApiResponse.<String>builder()
                .message("Successfully created password reset request")
                .build();
    }

    @PostMapping("/password-resets")
    ApiResponse<String> resetPassword(@RequestBody @Valid PasswordResetConfirmRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<String>builder()
                .message("Successfully reset password")
                .build();
    }
}
