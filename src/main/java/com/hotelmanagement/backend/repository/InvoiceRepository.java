package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,String> {
    Page<Invoice> findByInvoiceCodeContaining(
            String q,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "invoiceItems",
            "invoiceItems.extraService",
            "payments"
    })
    Optional<Invoice> findDetailById(String id);

    @Query("""
        select coalesce(
            sum(i.subtotal - i.discountAmount),
            0
        )
        from Invoice i
        where i.status = com.hotelmanagement.backend.enums.InvoiceStatus.DONE
            and i.paidAt between :start and :end
    """)
    BigDecimal getRevenueBetween(
            LocalDateTime start,
            LocalDateTime end
    );

}
