package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.dto.response.ExtraServiceResponse;
import com.hotelmanagement.backend.dto.response.InvoiceItemResponse;
import com.hotelmanagement.backend.dto.response.InvoicePromotionResponse;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceItemCreationData {
    Invoice invoice;
    InvoiceItemType type;
    long quantity;
    BigDecimal unitPrice;
    ExtraService extraService;
}
