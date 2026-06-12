package com.hotelmanagement.backend.dto.response;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsOverviewResponse {
    Integer todayBookings;

    Integer totalRooms;

    Integer availableRooms;

    Integer occupancyPct;

    BigDecimal weekRevenue;

    Integer newCustomers;

    Double bookingsDeltaPct;

    Integer occupancyDeltaPct;

    Integer availableRoomsDelta;

    Double weekRevenueDeltaPct;

    Integer newCustomersDelta;

    Integer totalCleanRooms;
}
