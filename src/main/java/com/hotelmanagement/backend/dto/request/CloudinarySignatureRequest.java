package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinarySignatureRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String context;

    @NotBlank(message = "REQUIRED_FIELD")
    String fileName;

    @NotBlank(message = "REQUIRED_FIELD")
    String contentType;

    @NotNull(message = "REQUIRED_FIELD")
    Long size;
}
