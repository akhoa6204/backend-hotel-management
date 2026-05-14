package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class InvoiceItemResponse {
    Long id;
    InvoiceItemType type;
    ExtraServiceResponse extraService;
    Integer quantity;
    BigDecimal unitPrice;
}
