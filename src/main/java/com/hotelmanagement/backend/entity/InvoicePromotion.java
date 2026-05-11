package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
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
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "invoiceId", nullable = false)
    Invoice invoice;

    Long promotionId;
    String promotionCode;
    String promotionName;

    @Enumerated(EnumType.STRING)
    DiscountType discountType;

    @Column(precision = 10, scale = 2)
    BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    BigDecimal discountAmount;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
