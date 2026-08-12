package com.hotelmanagement.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffShiftImportPreviewResponse {
    Summary summary;
    DateRange dateRange;
    List<StaffShiftImportRowResponse> rows;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        int totalRows;
        int validRows;
        int invalidRows;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateRange {
        LocalDate startDate;
        LocalDate endDate;
    }
}
