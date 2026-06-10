package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QuoteRequest {
    @NotNull(message = "REQUIRED_FIELD")
    Long roomId;
    String promotionCode;
    @NotNull(message = "REQUIRED_FIELD")
    LocalDate startDate;
    @NotNull(message = "REQUIRED_FIELD")
    LocalDate endDate;
}
