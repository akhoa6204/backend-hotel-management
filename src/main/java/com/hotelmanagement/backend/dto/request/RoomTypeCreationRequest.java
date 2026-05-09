package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    String name;
    @NotBlank
    String description;
    @Size(min = 1, max=10)
    int capacity;

    @NotBlank
    @Size(min = 1)
    BigDecimal basePrice;

    Set<String> amenities;
    Set<String> roomTypeImages;
}
