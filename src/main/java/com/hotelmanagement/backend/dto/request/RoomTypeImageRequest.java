package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomTypeImageRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String secureUrl;

    String publicId;

    String alt;
}
