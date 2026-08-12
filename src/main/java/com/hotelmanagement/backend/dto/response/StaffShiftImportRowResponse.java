package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.enums.StaffShiftValidationCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftImportRowResponse {
    Integer rowNumber;
    String email;
    String workDate;
    String shiftCode;
    String originalEmail;
    String originalWorkDate;
    String originalShiftCode;
    UserShortResponse employee;
    ShiftResponse shift;
    String status;
    List<StaffShiftValidationCode> validationErrors;
}
