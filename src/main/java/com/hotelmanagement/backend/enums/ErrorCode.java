package com.hotelmanagement.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1000, "Invalid message key", HttpStatus.BAD_REQUEST),

    FULLNAME_REQUIRED(1101, "Full name must not be blank", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1102, "User not found", HttpStatus.NOT_FOUND),

    EMAIL_REQUIRED(1201, "Email must not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1202, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1203, "Email already exists", HttpStatus.CONFLICT),

    PASSWORD_TOO_SHORT(1301, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),

    PHONE_REQUIRED(1401, "Phone is required", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1402, "Phone must be 10 digits", HttpStatus.BAD_REQUEST),

    INVALID_DOB(1501, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),

    ROOM_TYPE_ALREADY_EXISTS(1601, "Room type already exists", HttpStatus.CONFLICT),
    AMENITY_NOT_FOUND(1602, "Amenity not found", HttpStatus.NOT_FOUND),
    ROOM_TYPE_NOT_FOUND(1603, "Room type not found", HttpStatus.NOT_FOUND),

    ROOM_ALREADY_EXISTS(1701, "Room already exists", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND(1702, "Room not found", HttpStatus.NOT_FOUND),

    SERVICE_ALREADY_EXISTS(1801, "Service already exists", HttpStatus.CONFLICT),
    SERVICE_NOT_FOUND(1802, "Service not found", HttpStatus.NOT_FOUND),


    UNAUTHENTICATED(2001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "You do not have permission", HttpStatus.FORBIDDEN),
    PERMISSION_NOT_FOUND(2003, "Permission not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(2004, "Role not found", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
