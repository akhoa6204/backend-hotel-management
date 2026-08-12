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
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftService {
    private static final int MAX_SCHEDULE_PAGE_SIZE = 100;
    private static final Set<String> SCHEDULE_SORT_FIELDS = Set.of("name", "email", "position");

    ShiftRepository shiftRepository;
    UserRepository userRepository;
    StaffShiftAssignmentRepository staffShiftAssignmentRepository;
    UserService userService;
    ShiftMapper shiftMapper;
    StaffShiftAssignmentValidator assignmentValidator;
    public List<Shift> findAllDefinition() {
        return shiftRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<StaffShiftResponse> getSchedule(
            int page,
            int limit,
            String q,
            LocalDate startDate,
            LocalDate endDate,
            UserRole position,
            String sort
    ) {
        if (page < 0 || limit < 1 || limit > MAX_SCHEDULE_PAGE_SIZE || startDate.isAfter(endDate)) {
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }

        List<String> roleNames = position == null
                ? List.of(
                        UserRole.ADMIN.name(),
                        UserRole.MANAGER.name(),
                        UserRole.RECEPTIONIST.name(),
                        UserRole.HOUSEKEEPING.name()
                )
                : List.of(position.name());
        PageRequest pageRequest = PageRequest.of(page, limit, parseScheduleSort(sort));
        Page<User> staffPage = position == UserRole.USER
                ? Page.empty(pageRequest)
                : userRepository.findScheduleEmployees(roleNames, q == null ? "" : q.trim(), pageRequest);
        List<String> staffIds = staffPage.getContent().stream().map(User::getId).toList();
        List<StaffShiftAssignment> assignments = staffIds.isEmpty()
                ? List.of()
                : staffShiftAssignmentRepository.findScheduleAssignments(staffIds, startDate, endDate);
        List<StaffShiftResponse> rows = buildStaffShiftResponses(staffPage.getContent(), assignments);

        return new PageImpl<>(rows, pageRequest, staffPage.getTotalElements());
    }

    private Sort parseScheduleSort(String sort) {
        String[] parts = sort == null ? new String[0] : sort.split(",", -1);
        String requestedField = parts.length > 0 && !parts[0].isBlank() ? parts[0].trim() : "name";
        String requestedDirection = parts.length > 1 && !parts[1].isBlank() ? parts[1].trim() : "asc";
        if (!SCHEDULE_SORT_FIELDS.contains(requestedField)
                || !(requestedDirection.equalsIgnoreCase("asc") || requestedDirection.equalsIgnoreCase("desc"))) {
            throw new AppException(ErrorCode.INVALID_FORMAT);
        }

        String entityField = switch (requestedField) {
            case "name" -> "fullName";
            case "position" -> "role.name";
            default -> requestedField;
        };
        return Sort.by(Sort.Direction.fromString(requestedDirection), entityField)
                .and(Sort.by(Sort.Direction.ASC, "id"));
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

    @Transactional
    public void createStaffShift(StaffShiftCreationRequest request) {
        User staff = userRepository.findById(request.getStaffId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_NOT_FOUND));

        List<StaffShiftAssignment> existingAssignments =
                staffShiftAssignmentRepository.findByStaffIdAndWorkDate(staff.getId(), request.getWorkDate());
        var validationErrors = assignmentValidator.validate(
                staff, shift, request.getWorkDate(), existingAssignments
        );
        if (validationErrors.contains(com.hotelmanagement.backend.enums.StaffShiftValidationCode.EMPLOYEE_INELIGIBLE)) {
            throw new AppException(ErrorCode.SHIFT_STAFF_INELIGIBLE);
        }
        if (!validationErrors.isEmpty()) {
            throw new AppException(ErrorCode.SHIFT_ASSIGNMENT_CONFLICT);
        }

        StaffShiftAssignment assignment = StaffShiftAssignment.builder()
                .staff(staff)
                .shift(shift)
                .workDate(request.getWorkDate())
                .position(StaffPosition.fromUserRole(
                        UserRole.valueOf(staff.getRole().getName())
                ))
                .build();

        staffShiftAssignmentRepository.save(assignment);
    }

    public void deleteStaffShift(Integer id) {
        StaffShiftAssignment assignment = staffShiftAssignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff shift assignment not found"));

        staffShiftAssignmentRepository.delete(assignment);
    }
}
