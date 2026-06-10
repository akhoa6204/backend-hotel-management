package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResetPasswordRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String password;

}
