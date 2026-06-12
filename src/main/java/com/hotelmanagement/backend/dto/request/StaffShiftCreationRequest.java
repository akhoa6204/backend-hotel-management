package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.enums.StaffPosition;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffShiftCreationRequest {
    String staffId;
    Integer shiftId;
    LocalDate workDate;
}
