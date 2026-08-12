package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.StaffShiftCreationRequest;
import com.hotelmanagement.backend.dto.response.ShiftResponse;
import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.StaffPosition;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.repository.ShiftRepository;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    ShiftRepository shiftRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    StaffShiftAssignmentRepository staffShiftAssignmentRepository;

    @Mock
    UserService userService;

    @Mock
    ShiftMapper shiftMapper;

    @Spy
    StaffShiftAssignmentValidator assignmentValidator = new StaffShiftAssignmentValidator();

    @InjectMocks
    ShiftService shiftService;

    @org.junit.jupiter.api.Test
    void getSchedulePaginatesEmployeesAndLoadsAssignmentsForCurrentPageOnly() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        User first = staff("staff-a", "Ngoc", "ngoc@example.com", UserRole.HOUSEKEEPING);
        User second = staff("staff-b", "Bao", "bao@example.com", UserRole.HOUSEKEEPING);
        PageRequest expectedPage = PageRequest.of(0, 2,
                org.springframework.data.domain.Sort.by("fullName").ascending()
                        .and(org.springframework.data.domain.Sort.by("id").ascending()));
        when(userRepository.findScheduleEmployees(
                List.of(UserRole.HOUSEKEEPING.name()), "ngoc", expectedPage
        )).thenReturn(new PageImpl<>(List.of(first, second), expectedPage, 5));
        when(staffShiftAssignmentRepository.findScheduleAssignments(
                List.of("staff-a", "staff-b"), startDate, endDate
        )).thenReturn(List.of());

        var result = shiftService.getSchedule(
                0, 2, " ngoc ", startDate, endDate, UserRole.HOUSEKEEPING, "name,asc"
        );

        assertEquals(2, result.getContent().size());
        assertEquals(5, result.getTotalElements());
        verify(staffShiftAssignmentRepository).findScheduleAssignments(
                List.of("staff-a", "staff-b"), startDate, endDate
        );
    }

    @org.junit.jupiter.api.Test
    void getScheduleSearchIncludesEmailAndPositionFilteringInRepositoryQuery() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 7);
        PageRequest expectedPage = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("email").descending()
                        .and(org.springframework.data.domain.Sort.by("id").ascending()));
        when(userRepository.findScheduleEmployees(
                List.of(UserRole.RECEPTIONIST.name()), "tbngoc181203@gmail.com", expectedPage
        )).thenReturn(new PageImpl<>(List.of(), expectedPage, 0));

        var result = shiftService.getSchedule(
                0, 20, "tbngoc181203@gmail.com", startDate, endDate,
                UserRole.RECEPTIONIST, "email,desc"
        );

        assertEquals(0, result.getTotalElements());
        verify(staffShiftAssignmentRepository, never()).findScheduleAssignments(any(), any(), any());
    }

    @org.junit.jupiter.api.Test
    void getScheduleMapsOnlyAssignmentsReturnedForRequestedDateRange() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);
        User employee = staff("staff-a", "Ngoc", "ngoc@example.com", UserRole.HOUSEKEEPING);
        Shift morning = shift(1, 6, 14);
        StaffShiftAssignment augustAssignment = assignment(10, "staff-a", morning,
                LocalDate.of(2026, 8, 15));
        augustAssignment.setStaff(employee);
        PageRequest pageRequest = PageRequest.of(0, 20,
                org.springframework.data.domain.Sort.by("fullName").ascending()
                        .and(org.springframework.data.domain.Sort.by("id").ascending()));
        when(userRepository.findScheduleEmployees(any(), eq(""), eq(pageRequest)))
                .thenReturn(new PageImpl<>(List.of(employee), pageRequest, 1));
        when(staffShiftAssignmentRepository.findScheduleAssignments(
                List.of("staff-a"), startDate, endDate
        )).thenReturn(List.of(augustAssignment));
        when(shiftMapper.toShiftResponse(morning)).thenReturn(
                ShiftResponse.builder().id(1).code("MORNING").name("Morning").build()
        );

        var result = shiftService.getSchedule(
                0, 20, "", startDate, endDate, null, "name,asc"
        );

        assertEquals(1, result.getContent().get(0).getAssignments().size());
        assertEquals(LocalDate.of(2026, 8, 15),
                result.getContent().get(0).getAssignments().get(0).getWorkDate());
    }

    @org.junit.jupiter.api.Test
    void deleteStaffShiftStillDeletesExistingAssignment() {
        StaffShiftAssignment existing = StaffShiftAssignment.builder().id(10).build();
        when(staffShiftAssignmentRepository.findById(10)).thenReturn(Optional.of(existing));

        shiftService.deleteStaffShift(10);

        verify(staffShiftAssignmentRepository).delete(existing);
    }

    @ParameterizedTest
    @MethodSource("staffRoleMappings")
    void createStaffShiftMapsUserRoleToCanonicalStaffPosition(
            UserRole userRole,
            StaffPosition expectedPosition
    ) {
        String staffId = "staff-id";
        int shiftId = 1;
        LocalDate workDate = LocalDate.of(2026, 8, 10);
        User staff = User.builder()
                .id(staffId)
                .active(true)
                .role(Role.builder().name(userRole.name()).build())
                .build();
        Shift shift = Shift.builder().id(shiftId).build();
        StaffShiftCreationRequest request = StaffShiftCreationRequest.builder()
                .staffId(staffId)
                .shiftId(shiftId)
                .workDate(workDate)
                .build();

        when(userRepository.findById(staffId)).thenReturn(Optional.of(staff));
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));

        shiftService.createStaffShift(request);

        ArgumentCaptor<StaffShiftAssignment> assignmentCaptor =
                ArgumentCaptor.forClass(StaffShiftAssignment.class);
        verify(staffShiftAssignmentRepository).save(assignmentCaptor.capture());

        StaffShiftAssignment assignment = assignmentCaptor.getValue();
        assertEquals(staff, assignment.getStaff());
        assertEquals(shift, assignment.getShift());
        assertEquals(workDate, assignment.getWorkDate());
        assertEquals(expectedPosition, assignment.getPosition());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftRejectsDuplicateMasterShiftForSameStaffAndDate() {
        LocalDate workDate = LocalDate.of(2026, 8, 11);
        Shift morning = shift(1, 6, 14);
        StaffShiftCreationRequest request = request("staff-a", morning.getId(), workDate);
        mockStaffAndShift(request, morning);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-a", workDate))
                .thenReturn(List.of(assignment(10, "staff-a", morning, workDate)));

        AppException exception = assertThrows(AppException.class,
                () -> shiftService.createStaffShift(request));

        assertEquals(ErrorCode.SHIFT_ASSIGNMENT_CONFLICT, exception.getErrorCode());
        verify(staffShiftAssignmentRepository, never()).save(any());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftRejectsDifferentOverlappingShift() {
        LocalDate workDate = LocalDate.of(2026, 8, 11);
        Shift morning = shift(1, 6, 14);
        Shift overlapping = shift(2, 10, 18);
        StaffShiftCreationRequest request = request("staff-a", overlapping.getId(), workDate);
        mockStaffAndShift(request, overlapping);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-a", workDate))
                .thenReturn(List.of(assignment(10, "staff-a", morning, workDate)));

        AppException exception = assertThrows(AppException.class,
                () -> shiftService.createStaffShift(request));

        assertEquals(ErrorCode.SHIFT_ASSIGNMENT_CONFLICT, exception.getErrorCode());
        verify(staffShiftAssignmentRepository, never()).save(any());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftAllowsAdjacentShift() {
        LocalDate workDate = LocalDate.of(2026, 8, 11);
        Shift morning = shift(1, 6, 14);
        Shift afternoon = shift(2, 14, 22);
        StaffShiftCreationRequest request = request("staff-a", afternoon.getId(), workDate);
        mockStaffAndShift(request, afternoon);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-a", workDate))
                .thenReturn(List.of(assignment(10, "staff-a", morning, workDate)));

        shiftService.createStaffShift(request);

        verify(staffShiftAssignmentRepository).save(any());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftAllowsSameShiftForDifferentStaff() {
        LocalDate workDate = LocalDate.of(2026, 8, 11);
        Shift morning = shift(1, 6, 14);
        StaffShiftCreationRequest request = request("staff-b", morning.getId(), workDate);
        mockStaffAndShift(request, morning);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-b", workDate))
                .thenReturn(List.of());

        shiftService.createStaffShift(request);

        verify(staffShiftAssignmentRepository).save(any());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftAllowsSameShiftForSameStaffOnDifferentDate() {
        LocalDate workDate = LocalDate.of(2026, 8, 12);
        Shift morning = shift(1, 6, 14);
        StaffShiftCreationRequest request = request("staff-a", morning.getId(), workDate);
        mockStaffAndShift(request, morning);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-a", workDate))
                .thenReturn(List.of());

        shiftService.createStaffShift(request);

        verify(staffShiftAssignmentRepository).save(any());
    }

    @org.junit.jupiter.api.Test
    void createStaffShiftDetectsOverlapWithOvernightShift() {
        LocalDate workDate = LocalDate.of(2026, 8, 11);
        Shift overnight = shift(1, 22, 6);
        Shift overlapping = shift(2, 23, 7);
        StaffShiftCreationRequest request = request("staff-a", overlapping.getId(), workDate);
        mockStaffAndShift(request, overlapping);
        when(staffShiftAssignmentRepository.findByStaffIdAndWorkDate("staff-a", workDate))
                .thenReturn(List.of(assignment(10, "staff-a", overnight, workDate)));

        assertThrows(AppException.class, () -> shiftService.createStaffShift(request));
        verify(staffShiftAssignmentRepository, never()).save(any());
    }

    private void mockStaffAndShift(StaffShiftCreationRequest request, Shift shift) {
        User staff = User.builder()
                .id(request.getStaffId())
                .active(true)
                .role(Role.builder().name(UserRole.RECEPTIONIST.name()).build())
                .build();
        when(userRepository.findById(request.getStaffId())).thenReturn(Optional.of(staff));
        when(shiftRepository.findById(request.getShiftId())).thenReturn(Optional.of(shift));
    }

    private static StaffShiftCreationRequest request(String staffId, int shiftId, LocalDate workDate) {
        return StaffShiftCreationRequest.builder()
                .staffId(staffId)
                .shiftId(shiftId)
                .workDate(workDate)
                .build();
    }

    private static StaffShiftAssignment assignment(
            int id,
            String staffId,
            Shift shift,
            LocalDate workDate
    ) {
        return StaffShiftAssignment.builder()
                .id(id)
                .staff(User.builder().id(staffId).build())
                .shift(shift)
                .workDate(workDate)
                .position(StaffPosition.RECEPTION)
                .build();
    }

    private static Shift shift(int id, int startHour, int endHour) {
        return Shift.builder()
                .id(id)
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .build();
    }

    private static User staff(String id, String name, String email, UserRole role) {
        return User.builder()
                .id(id)
                .fullName(name)
                .email(email)
                .active(true)
                .role(Role.builder().name(role.name()).build())
                .build();
    }

    private static Stream<Arguments> staffRoleMappings() {
        return Stream.of(
                Arguments.of(UserRole.RECEPTIONIST, StaffPosition.RECEPTION),
                Arguments.of(UserRole.HOUSEKEEPING, StaffPosition.HOUSEKEEPING),
                Arguments.of(UserRole.MANAGER, StaffPosition.MANAGER),
                Arguments.of(UserRole.ADMIN, StaffPosition.ADMIN)
        );
    }
}
