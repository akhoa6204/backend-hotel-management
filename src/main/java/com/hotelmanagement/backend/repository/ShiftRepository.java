package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Integer> {
}
