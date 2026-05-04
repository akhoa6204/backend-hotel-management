package com.hotelmanagement.backend.enums;

public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Internal server error"),
    INVALID_KEY(1000, "Invalid message key"),

    FULLNAME_REQUIRED(1101, "Full name must not be blank"),

    EMAIL_REQUIRED(1201, "Email must not be blank"),
    EMAIL_INVALID(1202, "Invalid email format"),
    EMAIL_ALREADY_EXISTS(1203, "Email already exists"),

    PASSWORD_TOO_SHORT(1301, "Password must be at least 8 characters"),

    PHONE_REQUIRED(1401, "Phone is required"),
    PHONE_INVALID(1402, "Phone must be 10 digits"),

    USER_NOT_FOUND(2002, "User not found"),
    UNAUTHENTICATED(1002, "Unauthenticated");

    private int code;
    private String message;


     ErrorCode(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
