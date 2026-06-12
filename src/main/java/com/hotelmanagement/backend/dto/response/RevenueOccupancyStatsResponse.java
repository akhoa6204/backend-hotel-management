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
public class RevenueOccupancyStatsResponse {
    BigDecimal totalRevenue;

    Double totalRevenueDeltaPct;

    Integer occupancyPct;

    Double occupancyDeltaPct;
}
