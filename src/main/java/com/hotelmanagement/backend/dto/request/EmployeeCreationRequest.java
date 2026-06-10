package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreationRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String fullName;

    @NotNull(message = "REQUIRED_FIELD")
    String phone;

    @NotBlank(message = "REQUIRED_FIELD")
    String email;

    @NotNull(message = "REQUIRED_FIELD")
    UserRole role;
}
