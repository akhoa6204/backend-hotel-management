package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.StaffShiftCreationRequest;
import com.hotelmanagement.backend.dto.response.AssignmentInfoResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.StaffPosition;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.repository.ShiftRepository;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftService {
    ShiftRepository shiftRepository;
    UserRepository userRepository;
    StaffShiftAssignmentRepository staffShiftAssignmentRepository;
    UserService userService;
    ShiftMapper shiftMapper;
    public List<Shift> findAllDefinition() {
        return shiftRepository.findAll();
    }

    public List<StaffShiftResponse> getSchedule(
            String q,
            LocalDate startDate,
            LocalDate endDate,
            UserRole position
    ) {
        List<String> roleNames = position == null
                ? List.of(
                        UserRole.ADMIN.name(),
                        UserRole.RECEPTIONIST.name(),
                        UserRole.HOUSEKEEPING.name()
                )
                : List.of(position.name());

        List<User> staffs = userRepository.findByRole_NameInAndActiveTrue(roleNames);

        if (q != null && !q.isBlank()) {
            String keyword = q.trim().toLowerCase();

            staffs = staffs.stream()
                    .filter(user -> user.getFullName() != null
                            && user.getFullName().toLowerCase().contains(keyword))
                    .toList();
        }

        List<StaffShiftAssignment> assignments =
                staffShiftAssignmentRepository.findByWorkDateBetween(startDate, endDate);

        return buildStaffShiftResponses(staffs, assignments);
    }

    public List<StaffShiftResponse> getMySchedule(
            LocalDate startDate,
            LocalDate endDate
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userService.getById(userId);

        List<StaffShiftAssignment> assignments =
                staffShiftAssignmentRepository.findByStaffIdAndWorkDateBetween(
                        currentUser.getId(),
                        startDate,
                        endDate
                );

        return buildStaffShiftResponses(List.of(currentUser), assignments);
    }

    private List<StaffShiftResponse> buildStaffShiftResponses(
            List<User> staffs,
            List<StaffShiftAssignment> assignments
    ) {
        Map<String, List<StaffShiftAssignment>> assignmentMap =
                assignments.stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getStaff().getId()
                        ));

        return staffs.stream()
                .map(user -> StaffShiftResponse.builder()
                        .staff(
                                UserShortResponse.builder()
                                        .id(user.getId())
                                        .fullName(user.getFullName())
                                        .phone(user.getPhone())
                                        .email(user.getEmail())
                                        .roleName(user.getRole().getName())
                                        .build()
                        )
                        .assignments(
                                assignmentMap
                                        .getOrDefault(
                                                user.getId(),
                                                List.of()
                                        )
                                        .stream()
                                        .map(item ->
                                                AssignmentInfoResponse.builder()
                                                        .id(item.getId())
                                                        .staffId(item.getStaff().getId())
                                                        .workDate(item.getWorkDate())
                                                        .position(item.getPosition().name())
                                                        .shift(shiftMapper.toShiftResponse(item.getShift()))
                                                        .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();
    }

    public void createStaffShift(StaffShiftCreationRequest request) {
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new RuntimeException("Shift not found"));

        StaffShiftAssignment assignment = StaffShiftAssignment.builder()
                .staff(staff)
                .shift(shift)
                .workDate(request.getWorkDate())
                .position(StaffPosition.valueOf(staff.getRole().getName()))
                .build();

        staffShiftAssignmentRepository.save(assignment);
    }
    public void deleteStaffShift(Integer id) {
        StaffShiftAssignment assignment = staffShiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff shift assignment not found"));

        staffShiftAssignmentRepository.delete(assignment);
    }
}
