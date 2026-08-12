package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.StaffShiftImportConfirmRequest;
import com.hotelmanagement.backend.dto.request.StaffShiftImportRowRequest;
import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.StaffPosition;
import com.hotelmanagement.backend.enums.StaffShiftValidationCode;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.repository.ShiftRepository;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StaffShiftImportServiceTest {
    @Mock StaffShiftExcelParser excelParser;
    @Mock UserRepository userRepository;
    @Mock ShiftRepository shiftRepository;
    @Mock StaffShiftAssignmentRepository assignmentRepository;
    @Mock UserMapper userMapper;
    @Mock ShiftMapper shiftMapper;
    @Spy StaffShiftAssignmentValidator assignmentValidator = new StaffShiftAssignmentValidator();
    @InjectMocks StaffShiftImportService importService;

    User staff;
    Shift morning;

    @BeforeEach
    void setUp() {
        staff = User.builder().id("staff-a").fullName("Ngoc").email("ngoc@example.com")
                .active(true).role(Role.builder().name(UserRole.HOUSEKEEPING.name()).build()).build();
        morning = shift(1, "MORNING", 6, 14);
    }

    @Test
    void validImportRowPassesPreview() {
        mockResolvedData(List.of());

        var preview = importService.revalidate(request(row("ngoc@example.com", "2026-08-11", "MORNING")));

        assertEquals(1, preview.getSummary().getValidRows());
        assertEquals("VALID", preview.getRows().get(0).getStatus());
    }

    @Test
    void invalidEmployeeIsReported() {
        when(userRepository.findByNormalizedEmailIn(any())).thenReturn(List.of());
        when(shiftRepository.findByCodeIn(any())).thenReturn(List.of(morning));

        var preview = importService.revalidate(request(row("missing@example.com", "2026-08-11", "MORNING")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.EMPLOYEE_NOT_FOUND));
    }

    @Test
    void invalidShiftIsReported() {
        when(userRepository.findByNormalizedEmailIn(any())).thenReturn(List.of(staff));
        when(shiftRepository.findByCodeIn(any())).thenReturn(List.of());
        when(assignmentRepository.findScheduleAssignments(any(), any(), any())).thenReturn(List.of());

        var preview = importService.revalidate(request(row("ngoc@example.com", "2026-08-11", "UNKNOWN")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.SHIFT_NOT_FOUND));
    }

    @Test
    void invalidDateIsReported() {
        when(userRepository.findByNormalizedEmailIn(any())).thenReturn(List.of(staff));
        when(shiftRepository.findByCodeIn(any())).thenReturn(List.of(morning));

        var preview = importService.revalidate(request(row("ngoc@example.com", "31-08-2026", "MORNING")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.INVALID_DATE));
    }

    @Test
    void duplicateExistingScheduleIsReported() {
        mockResolvedData(List.of(assignment(morning)));

        var preview = importService.revalidate(request(row("ngoc@example.com", "2026-08-11", "MORNING")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.DUPLICATE_ASSIGNMENT));
    }

    @Test
    void duplicateRowsInsideExcelAreReported() {
        mockResolvedData(List.of());
        StaffShiftImportRowRequest first = row("ngoc@example.com", "2026-08-11", "MORNING");
        StaffShiftImportRowRequest second = row("ngoc@example.com", "2026-08-11", "MORNING");
        second.setRowNumber(3);

        var preview = importService.revalidate(request(first, second));

        assertEquals("VALID", preview.getRows().get(0).getStatus());
        assertTrue(preview.getRows().get(1).getValidationErrors()
                .contains(StaffShiftValidationCode.DUPLICATE_ASSIGNMENT));
    }

    @Test
    void inactiveEmployeeIsReportedAsIneligible() {
        staff.setActive(false);
        mockResolvedData(List.of());

        var preview = importService.revalidate(request(row("ngoc@example.com", "2026-08-11", "MORNING")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.EMPLOYEE_INELIGIBLE));
    }

    @Test
    void overlappingExistingScheduleIsReported() {
        Shift overlapping = shift(2, "OVERLAP", 10, 18);
        when(userRepository.findByNormalizedEmailIn(any())).thenReturn(List.of(staff));
        when(shiftRepository.findByCodeIn(any())).thenReturn(List.of(overlapping));
        when(assignmentRepository.findScheduleAssignments(any(), any(), any()))
                .thenReturn(List.of(assignment(morning)));

        var preview = importService.revalidate(request(row("ngoc@example.com", "2026-08-11", "OVERLAP")));

        assertTrue(preview.getRows().get(0).getValidationErrors()
                .contains(StaffShiftValidationCode.OVERLAPPING_SHIFT));
    }

    @Test
    void successfulConfirmSavesAllValidatedRowsInBatch() {
        mockResolvedData(List.of());
        StaffShiftImportConfirmRequest request = request(
                row("ngoc@example.com", "2026-08-11", "MORNING"),
                row("ngoc@example.com", "2026-08-12", "MORNING")
        );

        var result = importService.confirm(request);

        assertEquals(2, result.getImported());
        verify(assignmentRepository).saveAll(any());
    }

    private void mockResolvedData(List<StaffShiftAssignment> existing) {
        when(userRepository.findByNormalizedEmailIn(any())).thenReturn(List.of(staff));
        when(shiftRepository.findByCodeIn(any())).thenReturn(List.of(morning));
        when(assignmentRepository.findScheduleAssignments(any(), any(), any())).thenReturn(existing);
    }

    private StaffShiftAssignment assignment(Shift shift) {
        return StaffShiftAssignment.builder().staff(staff).shift(shift)
                .workDate(LocalDate.of(2026, 8, 11)).position(StaffPosition.HOUSEKEEPING).build();
    }

    private StaffShiftImportConfirmRequest request(StaffShiftImportRowRequest... rows) {
        return StaffShiftImportConfirmRequest.builder().rows(List.of(rows)).build();
    }

    private StaffShiftImportRowRequest row(String email, String date, String code) {
        return StaffShiftImportRowRequest.builder().rowNumber(2).email(email).workDate(date).shiftCode(code).build();
    }

    private Shift shift(int id, String code, int startHour, int endHour) {
        return Shift.builder().id(id).code(code).name(code)
                .startTime(LocalTime.of(startHour, 0)).endTime(LocalTime.of(endHour, 0)).build();
    }
}
