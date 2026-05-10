package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.DiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionCreationRequest {

    @NotBlank(message = "REQUIRED_INVALID")
    String name;

    @NotBlank(message = "REQUIRED_INVALID")
    String description;

    String code;

    @NotNull(message = "REQUIRED_INVALID")
    DiscountType discountType;

    @NotNull(message = "REQUIRED_INVALID")
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal discountValue;

    @NotNull(message = "REQUIRED_INVALID")
    LocalDate startDate;

    @NotNull(message = "REQUIRED_INVALID")
    LocalDate endDate;

    @Min(1)
    @Max(100)
    int priority;

    boolean stackable;
    boolean autoApplied;

    @Min(1)
    int quotaTotal;

    @NotNull
    DiscountScope scope;

    @DecimalMin(value = "0.0")
    BigDecimal minTotal;

    @DecimalMin(value = "0.0")
    BigDecimal maxDiscountAmount;
}
