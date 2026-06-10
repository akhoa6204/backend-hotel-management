package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.HousekeepingTaskType;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class HousekeepingTaskCreationRequest {
    @NotNull
    Long roomId;

    String staffId;

    @NotNull
    HousekeepingTaskType type;

    String bookingId;
}
