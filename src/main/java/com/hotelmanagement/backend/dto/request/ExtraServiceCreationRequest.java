package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExtraServiceCreationRequest {
    @NotBlank
    String name;

    @NotBlank
    String description;

    @NotBlank
    @Size(min = 1)
    BigDecimal basePrice;

    @NotBlank
    ServiceType type;
}
