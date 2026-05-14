package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentType;
import com.hotelmanagement.backend.validator.DobConstraint;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreationRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String invoiceId;

    @NotNull(message = "REQUIRED_FIELD")
    PaymentMethod paymentMethod;

    @NotNull(message = "REQUIRED_FIELD")
    PaymentType paymentType;

    @NotNull(message = "REQUIRED_FIELD")
    BigDecimal amount;
}
