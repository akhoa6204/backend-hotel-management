package com.hotelmanagement.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftImportRowRequest {
    Integer rowNumber;
    String email;
    String workDate;
    String shiftCode;
    String originalEmail;
    String originalWorkDate;
    String originalShiftCode;
}
