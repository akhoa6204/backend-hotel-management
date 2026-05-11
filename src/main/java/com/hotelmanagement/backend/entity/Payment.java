package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String paymentCode;

    @ManyToOne
    @JoinColumn(name = "invoiceId", nullable = false)
    Invoice invoice;

    @Enumerated(EnumType.STRING)
    PaymentMethod method;

    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    @Enumerated(EnumType.STRING)
    PaymentType type;

    @Column(precision = 10, scale = 2)
    BigDecimal amount;

    String transactionCode;

    LocalDateTime paidAt;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}