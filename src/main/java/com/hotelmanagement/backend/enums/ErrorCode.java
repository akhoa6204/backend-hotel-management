package com.hotelmanagement.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1000, "Invalid message key", HttpStatus.BAD_REQUEST),

    FULLNAME_REQUIRED(1101, "Full name must not be blank", HttpStatus.BAD_REQUEST),

    EMAIL_REQUIRED(1201, "Email must not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1202, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1203, "Email already exists", HttpStatus.CONFLICT),

    PASSWORD_TOO_SHORT(1301, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),

    PHONE_REQUIRED(1401, "Phone is required", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1402, "Phone must be 10 digits", HttpStatus.BAD_REQUEST),

    USER_NOT_FOUND(2002, "User not found", HttpStatus.NOT_FOUND),

    UNAUTHENTICATED(1002, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
