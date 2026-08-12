package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.enums.StaffPosition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface StaffShiftAssignmentRepository extends JpaRepository<StaffShiftAssignment, Integer> {
    @EntityGraph(attributePaths = "shift")
    List<StaffShiftAssignment> findByWorkDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    @EntityGraph(attributePaths = "shift")
    List<StaffShiftAssignment> findByStaffIdAndWorkDateBetween(
            String staffId,
            LocalDate startDate,
            LocalDate endDate
    );

    @EntityGraph(attributePaths = "shift")
    List<StaffShiftAssignment> findByStaffIdAndWorkDate(
            String staffId,
            LocalDate workDate
    );

    @EntityGraph(attributePaths = "shift")
    @Query("""
            SELECT assignment
            FROM StaffShiftAssignment assignment
            WHERE assignment.staff.id IN :staffIds
              AND assignment.workDate BETWEEN :startDate AND :endDate
            ORDER BY assignment.workDate, assignment.shift.startTime
            """)
    List<StaffShiftAssignment> findScheduleAssignments(
            @Param("staffIds") List<String> staffIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<StaffShiftAssignment> findByWorkDateAndPositionAndShift_StartTimeLessThanEqualAndShift_EndTimeGreaterThanEqual(
            LocalDate workDate,
            StaffPosition position,
            LocalTime startTime,
            LocalTime endTime
    );
}
