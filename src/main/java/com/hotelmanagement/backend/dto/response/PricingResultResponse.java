package com.hotelmanagement.backend.dto.response;

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
public class PricingResultResponse {
    long nights;

    BigDecimal basePrice;

    BigDecimal subtotal;

    BigDecimal totalDiscount;

    BigDecimal finalTotal;

    PromotionResponse promotion;

    BigDecimal promotionDiscount;

    PromotionResponse autoPromotion;

    BigDecimal autoDiscount;
}