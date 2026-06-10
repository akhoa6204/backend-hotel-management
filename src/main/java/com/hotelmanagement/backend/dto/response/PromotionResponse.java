package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.DiscountType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class PromotionResponse {
    Long id;
    String name;
    String description;
    String code;
    DiscountType discountType;
    BigDecimal discountValue;
    LocalDate startDate;
    LocalDate endDate;
    boolean active;
    int priority;
    boolean stackable;
    int quotaUsed;
    int quotaTotal;
    DiscountScope scope;
    BigDecimal minTotal;
    BigDecimal maxDiscountAmount;
    boolean autoApplied;
}
