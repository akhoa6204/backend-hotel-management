package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    boolean existsById(Long id);

    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.status = 'PENDING'
              AND p.expiredAt IS NOT NULL
              AND p.expiredAt <= :now
            """)
    List<Payment> findExpiredPendingPayments(
            @Param("now") LocalDateTime now
    );
    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.invoice.id = :invoiceId
              AND p.status = 'PENDING'
              AND p.id <> :excludePaymentId
            """)
    List<Payment> findOtherPendingPaymentsByInvoiceId(
            @Param("invoiceId") String invoiceId,
            @Param("excludePaymentId") Long excludePaymentId
    );

    @Query("""
            SELECT p
            FROM Payment p
            WHERE p.invoice.id = :invoiceId
              AND p.type = 'REFUND'
              AND p.status = 'PENDING'
            """)
    List<Payment> findPendingRefundPaymentsByInvoiceId(
            @Param("invoiceId") String invoiceId
    );
}
