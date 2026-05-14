package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.entity.Permisson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsById(Long id);
}
