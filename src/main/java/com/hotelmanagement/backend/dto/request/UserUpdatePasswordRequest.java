package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String password;

    @NotBlank(message = "REQUIRED_FIELD")
    @Size(min = 8, message = "PASSWORD_TOO_SHORT")
    String newPassword;

}
