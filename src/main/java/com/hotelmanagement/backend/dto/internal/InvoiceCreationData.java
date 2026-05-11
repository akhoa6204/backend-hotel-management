package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.dto.response.InvoiceItemResponse;
import com.hotelmanagement.backend.dto.response.InvoicePromotionResponse;
import com.hotelmanagement.backend.entity.Booking;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceCreationData {
    Booking booking;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal taxAmount;
    BigDecimal remainingAmount;
    Set<InvoiceItemResponse> invoiceItems;
    Set<InvoicePromotionResponse> promotions;
}
