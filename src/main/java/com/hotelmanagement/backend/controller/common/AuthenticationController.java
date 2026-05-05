package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.AuthenticationResponse;
import com.hotelmanagement.backend.dto.response.IntrospectResponse;
import com.hotelmanagement.backend.dto.response.UserResponse;
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

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .data(AuthenticationResponse.builder()
                        .authenticated(result.isAuthenticated())
                        .token(result.getToken())
                        .build())
                .build();
    }

    @PostMapping("/register")
    ApiResponse<UserResponse> register(@RequestBody @Valid UserCreationRequest request) {
        ApiResponse<UserResponse> response = new ApiResponse<>();

        UserResponse userResponse = userService.createUser(request);

        response.setData(userResponse);
        return response;

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
}
