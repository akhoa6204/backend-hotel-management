package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @Column(unique = true, nullable = false)
    String bookingCode;

    @ManyToOne
    @JoinColumn(name = "roomId")
    Room room;

    @ManyToOne
    @JoinColumn(name = "customerId")
    User customer;

    @ManyToOne
    @JoinColumn(name = "staffId")
    User staff;

    @OneToOne(mappedBy = "booking")
    Invoice invoice;

    @OneToOne(mappedBy = "booking")
    Review review;

    @OneToOne(mappedBy = "booking")
    CancelReason cancelReason;

    LocalDate checkInDate;
    LocalDate checkOutDate;
    LocalTime estimatedArrivalTime;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    boolean bookingForSomeoneElse;
    String guestName;
    String guestPhone;
    String guestEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(8) default 'VI'")
    @Builder.Default
    BookingEmailLocale emailLocale = BookingEmailLocale.VI;

    boolean refundable;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

}
