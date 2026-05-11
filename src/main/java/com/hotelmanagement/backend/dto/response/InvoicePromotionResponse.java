package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class InvoicePromotionResponse {
    Long id;
    Long promotionId;
    String promotionCode;
    String promotionName;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal discountAmount;
}
