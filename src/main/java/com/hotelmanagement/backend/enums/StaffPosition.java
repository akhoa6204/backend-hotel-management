package com.hotelmanagement.backend.enums;

public enum StaffPosition {
    MANAGER,
    RECEPTION,
    HOUSEKEEPING,
    ADMIN;

    public static StaffPosition fromUserRole(UserRole role) {
        return switch (role) {
            case ADMIN -> ADMIN;
            case MANAGER -> MANAGER;
            case RECEPTIONIST -> RECEPTION;
            case HOUSEKEEPING -> HOUSEKEEPING;
            case USER -> throw new IllegalArgumentException(
                    "User role is not eligible for a staff shift: " + role
            );
        };
    }
}
