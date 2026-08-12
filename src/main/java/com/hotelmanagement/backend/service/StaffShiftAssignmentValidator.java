package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.StaffShiftValidationCode;
import com.hotelmanagement.backend.enums.UserRole;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class StaffShiftAssignmentValidator {
    public List<StaffShiftValidationCode> validate(
            User staff,
            Shift shift,
            LocalDate workDate,
            List<StaffShiftAssignment> assignments
    ) {
        List<StaffShiftValidationCode> errors = new ArrayList<>();
        if (!canHaveSchedule(staff)) {
            errors.add(StaffShiftValidationCode.EMPLOYEE_INELIGIBLE);
            return errors;
        }

        boolean duplicate = assignments.stream()
                .anyMatch(existing -> existing.getShift().getId().equals(shift.getId()));
        if (duplicate) {
            errors.add(StaffShiftValidationCode.DUPLICATE_ASSIGNMENT);
        }

        boolean overlap = assignments.stream()
                .filter(existing -> !existing.getShift().getId().equals(shift.getId()))
                .anyMatch(existing -> shiftsOverlap(workDate, existing.getShift(), shift));
        if (overlap) {
            errors.add(StaffShiftValidationCode.OVERLAPPING_SHIFT);
        }
        return errors;
    }

    public boolean canHaveSchedule(User staff) {
        if (staff == null || !staff.isActive() || staff.getRole() == null) {
            return false;
        }
        try {
            return UserRole.valueOf(staff.getRole().getName()) != UserRole.USER;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean shiftsOverlap(LocalDate workDate, Shift first, Shift second) {
        LocalDateTime firstStart = workDate.atTime(first.getStartTime());
        LocalDateTime firstEnd = workDate.atTime(first.getEndTime());
        if (!first.getEndTime().isAfter(first.getStartTime())) {
            firstEnd = firstEnd.plusDays(1);
        }

        LocalDateTime secondStart = workDate.atTime(second.getStartTime());
        LocalDateTime secondEnd = workDate.atTime(second.getEndTime());
        if (!second.getEndTime().isAfter(second.getStartTime())) {
            secondEnd = secondEnd.plusDays(1);
        }
        return secondStart.isBefore(firstEnd) && secondEnd.isAfter(firstStart);
    }
}
