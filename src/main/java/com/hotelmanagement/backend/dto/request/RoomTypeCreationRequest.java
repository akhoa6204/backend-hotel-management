package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeCreationRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String name;
    @NotBlank(message = "REQUIRED_FIELD")
    String description;

    @NotNull(message = "REQUIRED_FIELD")
    int capacity;

    @NotNull(message = "REQUIRED_FIELD")
    BigDecimal basePrice;

    Set<String> amenities;
    Set<String> roomTypeImages;
}
