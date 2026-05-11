package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.validator.DobConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    String fullName;

    @Email(message = "EMAIL_INVALID")
    String email;

    @Size(min = 8, message = "PASSWORD_TOO_SHORT")
    String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "PHONE_INVALID")
    String phone;

    LocalDate dob;

    List<String> roles;
}
