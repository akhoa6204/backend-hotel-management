package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.*;
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
public class BookingCreationRequest {
    @NotNull(message = "REQUIRED_FIELD")
    Long roomId;
    String customerId;
    String staffId;
    @NotNull(message = "REQUIRED_FIELD")

    @NotNull(message = "REQUIRED_FIELD")
    LocalDate checkInDate;
    @NotNull(message = "REQUIRED_FIELD")
    LocalDate checkOutDate;
    LocalTime estimatedArrivalTime;
    Boolean bookingForSomeoneElse;

    @NotBlank(message = "REQUIRED_FIELD")
    String guestName;
    @NotBlank(message = "REQUIRED_FIELD")
    @Pattern(regexp = "^[0-9]{10}$", message = "PHONE_INVALID")
    String guestPhone;

    String guestEmail;

    String promotionCode;
}
