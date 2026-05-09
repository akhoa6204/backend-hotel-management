package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.DiscountType;
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
public class PromotionUpdateRequest {
    @NotBlank
    String name;

    @NotBlank
    String description;

    String code;

    @NotNull
    DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal discountValue;

    @NotNull
    LocalDate startDate;

    @NotNull
    LocalDate endDate;

    @Min(1)
    @Max(100)
    int priority;

    boolean stackable;

    @Min(1)
    int quotaTotal;

    @NotNull
    DiscountScope scope;

    @DecimalMin(value = "0.0")
    BigDecimal minTotal;

    @DecimalMin(value = "0.0")
    BigDecimal maxDiscountAmount;
}
