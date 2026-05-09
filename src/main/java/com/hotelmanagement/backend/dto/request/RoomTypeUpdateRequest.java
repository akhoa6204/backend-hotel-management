package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeUpdateRequest {
    @NotBlank
    String name;
    @NotBlank
    String description;
    @Size(min = 1)
    int capacity;

    @NotBlank
    BigDecimal basePrice;

    Set<String> amenities;
    Set<String> roomTypeImages;
}
