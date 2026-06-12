package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.enums.StaffPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface StaffShiftAssignmentRepository extends JpaRepository<StaffShiftAssignment, Integer> {
    List<StaffShiftAssignment> findByWorkDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<StaffShiftAssignment> findByStaffIdAndWorkDateBetween(
            String staffId,
            LocalDate startDate,
            LocalDate endDate
    );

    List<StaffShiftAssignment> findByWorkDateAndPositionAndShift_StartTimeLessThanEqualAndShift_EndTimeGreaterThanEqual(
            LocalDate workDate,
            StaffPosition position,
            LocalTime startTime,
            LocalTime endTime
    );
}
