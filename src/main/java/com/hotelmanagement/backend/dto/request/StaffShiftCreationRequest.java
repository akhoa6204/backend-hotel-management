package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffShiftCreationRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String staffId;

    @NotNull(message = "REQUIRED_FIELD")
    @Positive(message = "INVALID_FORMAT")
    Integer shiftId;

    @NotNull(message = "REQUIRED_FIELD")
    LocalDate workDate;
}
