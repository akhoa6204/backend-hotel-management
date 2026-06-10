package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class BookingResponse {
    String id;
    String bookingCode;
    String invoiceId;
    RoomShortResponse room;
    UserShortResponse staff;
    UserShortResponse customer ;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    LocalTime estimatedArrivalTime;
    BookingStatus status;
    boolean bookingForSomeoneElse;
    String guestName;
    String guestPhone;
    String guestEmail;
    boolean refundable;

    boolean inspected;
    Long inspectionTaskId;
}
