package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class InvoiceResponse {
    String id;
    String invoiceCode;
    String bookingId;
    InvoiceStatus status;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal taxAmount;
    BigDecimal remainingAmount;
    LocalDateTime issuedAt;
    LocalDateTime paidAt;
    Set<InvoiceItemResponse> invoiceItems;
    Set<PaymentResponse> payments;
    Set<InvoicePromotionResponse> promotions;
}
