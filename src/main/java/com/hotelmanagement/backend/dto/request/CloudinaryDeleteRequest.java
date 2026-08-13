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
public class CloudinaryDeleteRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String publicId;

    @NotBlank(message = "REQUIRED_FIELD")
    String context;
}
