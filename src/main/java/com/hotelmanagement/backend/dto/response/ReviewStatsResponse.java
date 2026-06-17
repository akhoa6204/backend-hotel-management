package com.hotelmanagement.backend.dto.response;


import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewStatsResponse {

    Double avgOverall;

    Double avgAmenities;

    Double avgCleanliness;

    Double avgComfort;

    Double avgLocationScore;

    Double avgValueForMoney;

    Double avgHygiene;

    Long totalActiveReviews;

    Long totalHiddenReviews;

}