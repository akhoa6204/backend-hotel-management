package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {
    String fullName;

    String email;

    String phone;

    UserRole role;

    boolean active;
}
