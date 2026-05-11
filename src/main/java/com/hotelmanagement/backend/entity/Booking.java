package com.hotelmanagement.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.DiscountType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

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

    LocalDate checkInDate;
    LocalDate checkOutDate;
    LocalTime estimatedArrivalTime;

    @Enumerated(EnumType.STRING)
    BookingStatus status;

    boolean bookingForSomeoneElse;
    String guestName;
    String guestPhone;
    String guestEmail;

    boolean refundable;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;

}
