package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetConfirmRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String token;

    @NotBlank(message = "REQUIRED_FIELD")
    String password;
}
