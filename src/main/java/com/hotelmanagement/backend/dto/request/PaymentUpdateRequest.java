package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
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
public class PaymentUpdateRequest {
    PaymentStatus status;
    String transactionCode;
}
