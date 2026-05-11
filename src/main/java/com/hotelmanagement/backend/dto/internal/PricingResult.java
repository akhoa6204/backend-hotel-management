package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.entity.Promotion;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PricingResult {
    long nights;

    BigDecimal subtotal;

    BigDecimal totalDiscount;

    BigDecimal finalTotal;

    Promotion promotion;

    BigDecimal promotionDiscount;

    Promotion autoPromotion;

    BigDecimal autoDiscount;
}