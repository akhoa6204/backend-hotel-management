package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.enums.InvoiceStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceUpdateData {
    InvoiceStatus invoiceStatus;
}
