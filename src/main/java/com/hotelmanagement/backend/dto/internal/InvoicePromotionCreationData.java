package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.dto.response.InvoiceItemResponse;
import com.hotelmanagement.backend.dto.response.InvoicePromotionResponse;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoicePromotionCreationData {
    Invoice invoice;
    Long promotionId;
    String promotionCode;
    String promotionName;
    DiscountType discountType;
    BigDecimal discountValue;
    BigDecimal discountAmount;
}
