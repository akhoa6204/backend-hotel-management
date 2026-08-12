package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingCreationData {
    Room room;
    User customer;
    User staff;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    LocalTime estimatedArrivalTime;
    Boolean bookingForSomeoneElse;
    String guestName;
    String guestPhone;
    String guestEmail;
    BookingEmailLocale emailLocale;
}
