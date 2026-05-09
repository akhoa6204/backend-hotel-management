package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long Id;
    String name;
    String description;
    String code;
    @Enumerated(EnumType.STRING)
    DiscountType discountType;
    @Column(precision = 10, scale = 2)
    BigDecimal discountValue;
    LocalDate startDate;
    LocalDate endAt;
    boolean active;
    int priority;
    boolean stackable;
    int quotaUsed;
    int quotaTotal;
    @Enumerated(EnumType.STRING)
    DiscountScope scope;
    int minTotal;
    int maxDiscountAmount;

    @CreationTimestamp
    Date createdAt;

    @UpdateTimestamp
    Date updatedAt;
}
