package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String invoiceCode;

    @OneToOne
    @JoinColumn(name = "bookingId", nullable = false)
    Booking booking;

    @OneToMany(mappedBy = "invoice")
    Set<InvoiceItem> invoiceItems;

    @OneToMany(mappedBy = "invoice")
    Set<Payment> payments;

    @OneToMany(mappedBy = "invoice")
    Set<InvoicePromotion> invoicePromotions;

    @Enumerated(EnumType.STRING)
    InvoiceStatus status;

    @Column(precision = 10, scale = 2)
    BigDecimal subtotal;


    @Column(precision = 10, scale = 2)
    BigDecimal discountAmount;

    @Column(precision = 10, scale = 2)
    BigDecimal taxAmount;

    @Column(precision = 10, scale = 2)
    BigDecimal remainingAmount;

    LocalDateTime issuedAt;
    LocalDateTime paidAt;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
