package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class PaymentResponse {
    Long id;
    String invoiceId;
    String invoiceCode;
    String paymentCode;
    PaymentMethod method;
    PaymentStatus status;
    PaymentType type;
    BigDecimal amount;
    String transactionCode;
    LocalDateTime paidAt;
    LocalDateTime expiredAt;
}
