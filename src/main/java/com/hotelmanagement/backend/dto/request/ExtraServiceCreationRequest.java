package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.ServiceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    @NotBlank(message = "REQUIRED_FIELD")
    String name;

    @NotBlank(message = "REQUIRED_FIELD")
    String description;

    @NotNull(message = "REQUIRED_FIELD")
    BigDecimal basePrice;

    @NotNull(message = "REQUIRED_FIELD")
    ServiceType type;
}
