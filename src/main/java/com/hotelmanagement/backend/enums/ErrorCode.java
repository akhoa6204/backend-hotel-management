package com.hotelmanagement.backend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_KEY(1000, "Invalid message key", HttpStatus.BAD_REQUEST),
    REQUIRED_FIELD(1001, "%s is required", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT(1002, "%s is invalid", HttpStatus.BAD_REQUEST),

    FULLNAME_REQUIRED(1101, "Full name must not be blank", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(1102, "User not found", HttpStatus.NOT_FOUND),

    EMAIL_REQUIRED(1201, "Email must not be blank", HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1202, "Invalid email format", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_EXISTS(1203, "Email already exists", HttpStatus.CONFLICT),

    PASSWORD_TOO_SHORT(1301, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    PASSWORD_INCORRECT(1302, "Current password is incorrect", HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1303, "Invalid or expired token", HttpStatus.BAD_REQUEST),
    PASSWORD_RESET_TOKEN_NOT_FOUND(1304, "Password reset token not found", HttpStatus.NOT_FOUND),

    PHONE_REQUIRED(1401, "Phone is required", HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1402, "Phone must be 10 digits", HttpStatus.BAD_REQUEST),

    INVALID_DOB(1501, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),

    ROOM_TYPE_ALREADY_EXISTS(1601, "Room type already exists", HttpStatus.CONFLICT),
    AMENITY_NOT_FOUND(1602, "Amenity not found", HttpStatus.NOT_FOUND),
    ROOM_TYPE_NOT_FOUND(1603, "Room type not found", HttpStatus.NOT_FOUND),

    ROOM_ALREADY_EXISTS(1701, "Room already exists", HttpStatus.CONFLICT),
    ROOM_NOT_FOUND(1702, "Room not found", HttpStatus.NOT_FOUND),


    PROMOTION_ALREADY_EXISTS(1801, "Promotion already exists", HttpStatus.CONFLICT),
    PROMOTION_NOT_FOUND(1802, "Promotion not found", HttpStatus.NOT_FOUND),
    PROMOTION_QUOTA_EXCEEDED(1803, "Promotion usage limit exceeded", HttpStatus.BAD_REQUEST),
    PROMOTION_EXPIRED(1804, "Promotion has expired", HttpStatus.BAD_REQUEST),
    PROMOTION_NOT_STARTED(1805, "Promotion has not started yet", HttpStatus.BAD_REQUEST),

    SERVICE_ALREADY_EXISTS(2101, "Service already exists", HttpStatus.CONFLICT),
    SERVICE_NOT_FOUND(2102, "Service not found", HttpStatus.NOT_FOUND),

    INVALID_BOOKING_DATE(1901, "Check-out date must be after check-in date", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_EXISTS(1902, "Room is already booked for selected dates", HttpStatus.BAD_REQUEST),
    BOOKING_NOT_FOUND(1903, "Booking not found", HttpStatus.NOT_FOUND),

    INVOICE_ALREADY_EXISTS(2101, "Invoice is already exists", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_FOUND(2102, "Invoice not found", HttpStatus.NOT_FOUND),
    INVOICE_ITEM_ALREADY_EXISTS(2103, "Invoice item is already exists", HttpStatus.BAD_REQUEST),
    INVOICE_ITEM_NOT_FOUND(2104, "Invoice item not found", HttpStatus.NOT_FOUND),
    PAYMENT_NOT_FOUND(2105, "Payment not found", HttpStatus.NOT_FOUND),
    PAYMENT_ALREADY_EXISTS(2106, "Payment is already exists", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_FULLY_PAID(2107, "Invoice is not fully paid", HttpStatus.BAD_REQUEST),
    INVOICE_ALREADY_PAID(2108, "Invoice already paid", HttpStatus.BAD_REQUEST),
    PAYMENT_REQUIRED_TO_CONFIRM_BOOKING(2109, "Payment required to confirm booking", HttpStatus.BAD_REQUEST),
    ROOM_PAYMENT_REQUIRED_FOR_CHECKIN(2110, "Room payment required for check-in", HttpStatus.BAD_REQUEST),
    INVALID_BOOKING_STATUS(2111, "Invalid booking status", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_TRANSFER(2112, "Payment is not bank transfer", HttpStatus.BAD_REQUEST),
    PAYMENT_INVALID_STATUS(2113, "Payment status is invalid", HttpStatus.BAD_REQUEST),
    BOOKING_ALREADY_CANCELLED(2114, "Booking is already cancelled", HttpStatus.BAD_REQUEST),
    CANCEL_REASON_ALREADY_EXISTS(2115, "Cancel reason already exists", HttpStatus.BAD_REQUEST),

    HOUSEKEEPING_TASK_NOT_FOUND(2201, "Housekeeping task not found", HttpStatus.NOT_FOUND),
    HOUSEKEEPING_TASK_ALREADY_EXISTS(2202, "Housekeeping task is already exists", HttpStatus.BAD_REQUEST),
    INSPECTION_TASK_REQUIRED_FOR_CHECKOUT(2203, "Inspection task must be completed before checkout", HttpStatus.BAD_REQUEST),

    SHIFT_NOT_FOUND(2301, "Shift not found", HttpStatus.NOT_FOUND),
    SHIFT_ASSIGNMENT_CONFLICT(2302, "Staff shift assignment conflicts with an existing assignment", HttpStatus.CONFLICT),
    SHIFT_STAFF_INELIGIBLE(2303, "Employee is not eligible for staff scheduling", HttpStatus.BAD_REQUEST),
    SHIFT_IMPORT_INVALID(2304, "Staff schedule import contains invalid rows", HttpStatus.BAD_REQUEST),

    UNAUTHENTICATED(2001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "You do not have permission", HttpStatus.FORBIDDEN),
    PERMISSION_NOT_FOUND(2003, "Permission not found", HttpStatus.NOT_FOUND),
    NAME_REQUIRED(2005, "Name is required", HttpStatus.BAD_REQUEST),
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
