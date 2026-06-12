package com.hotelmanagement.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyBookingStatsResponse {

    String month;

    Integer total;

    Integer success;

    Integer cancelled;

    Integer cancelRate;
}
