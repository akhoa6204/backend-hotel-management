package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.BookingEmailLocale;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String email;

    @Builder.Default
    BookingEmailLocale locale = BookingEmailLocale.VI;
}
