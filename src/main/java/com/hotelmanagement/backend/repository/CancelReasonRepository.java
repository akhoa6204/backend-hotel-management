package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.CancelReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancelReasonRepository extends JpaRepository<CancelReason, Long> {
    boolean existsByBookingId(String bookingId);
}
