package com.hotelmanagement.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewResponse {
    String id;
    String bookingId;
    Integer overall;
    Integer amenities;
    Integer cleanliness;
    Integer comfort;
    Integer locationScore;
    Integer valueForMoney;
    Integer hygiene;
    String comment;
    BookingResponse booking;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    boolean active;
}
