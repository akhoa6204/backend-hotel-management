package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.StaffShiftImportConfirmRequest;
import com.hotelmanagement.backend.dto.request.StaffShiftImportRowRequest;
import com.hotelmanagement.backend.dto.response.ShiftResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftImportPreviewResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftImportResultResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftImportRowResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.entity.Shift;
import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.StaffPosition;
import com.hotelmanagement.backend.enums.StaffShiftValidationCode;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.repository.ShiftRepository;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffShiftImportService {
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("d/M/uuuu")
    );

    StaffShiftExcelParser excelParser;
    UserRepository userRepository;
    ShiftRepository shiftRepository;
    StaffShiftAssignmentRepository assignmentRepository;
    StaffShiftAssignmentValidator assignmentValidator;
    UserMapper userMapper;
    ShiftMapper shiftMapper;

    @Transactional(readOnly = true)
    public StaffShiftImportPreviewResponse preview(MultipartFile file) {
        return validateRows(excelParser.parse(file)).preview();
    }

    @Transactional(readOnly = true)
    public StaffShiftImportPreviewResponse revalidate(StaffShiftImportConfirmRequest request) {
        return validateRows(safeRows(request)).preview();
    }

    @Transactional
    public StaffShiftImportResultResponse confirm(StaffShiftImportConfirmRequest request) {
        ValidationResult result = validateRows(safeRows(request));
        if (result.preview().getSummary().getInvalidRows() > 0) {
            throw new AppException(ErrorCode.SHIFT_IMPORT_INVALID);
        }
        assignmentRepository.saveAll(result.assignments());
        return StaffShiftImportResultResponse.builder().imported(result.assignments().size()).build();
    }

    private List<StaffShiftImportRowRequest> safeRows(StaffShiftImportConfirmRequest request) {
        return request == null || request.getRows() == null ? List.of() : request.getRows();
    }

    private ValidationResult validateRows(List<StaffShiftImportRowRequest> sourceRows) {
        List<ParsedRow> parsedRows = sourceRows.stream().map(this::parseRow).toList();
        List<String> emails = parsedRows.stream().map(ParsedRow::normalizedEmail)
                .filter(value -> !value.isBlank()).distinct().toList();
        List<String> shiftCodes = parsedRows.stream().map(ParsedRow::normalizedShiftCode)
                .filter(value -> !value.isBlank()).distinct().toList();
        Map<String, User> usersByEmail = emails.isEmpty() ? Map.of() : userRepository
                .findByNormalizedEmailIn(emails).stream()
                .collect(Collectors.toMap(user -> normalizeEmail(user.getEmail()), Function.identity(), (a, b) -> a));
        Map<String, Shift> shiftsByCode = shiftCodes.isEmpty() ? Map.of() : shiftRepository
                .findByCodeIn(shiftCodes).stream()
                .collect(Collectors.toMap(shift -> normalizeShiftCode(shift.getCode()), Function.identity(), (a, b) -> a));

        List<String> staffIds = parsedRows.stream().map(row -> usersByEmail.get(row.normalizedEmail()))
                .filter(Objects::nonNull).map(User::getId).distinct().toList();
        List<LocalDate> validDates = parsedRows.stream().map(ParsedRow::workDate)
                .filter(Objects::nonNull).toList();
        LocalDate startDate = validDates.stream().min(LocalDate::compareTo).orElse(null);
        LocalDate endDate = validDates.stream().max(LocalDate::compareTo).orElse(null);
        List<StaffShiftAssignment> existing = staffIds.isEmpty() || startDate == null
                ? List.of()
                : assignmentRepository.findScheduleAssignments(staffIds, startDate, endDate);
        Map<String, List<StaffShiftAssignment>> assignmentsByStaffDate = new HashMap<>();
        existing.forEach(assignment -> assignmentsByStaffDate
                .computeIfAbsent(key(assignment.getStaff().getId(), assignment.getWorkDate()), ignored -> new ArrayList<>())
                .add(assignment));

        List<StaffShiftImportRowResponse> responseRows = new ArrayList<>();
        List<StaffShiftAssignment> preparedAssignments = new ArrayList<>();
        for (ParsedRow row : parsedRows) {
            User staff = usersByEmail.get(row.normalizedEmail());
            Shift shift = shiftsByCode.get(row.normalizedShiftCode());
            Set<StaffShiftValidationCode> errors = new LinkedHashSet<>(row.parsingErrors());
            if (staff == null) errors.add(StaffShiftValidationCode.EMPLOYEE_NOT_FOUND);
            if (shift == null) errors.add(StaffShiftValidationCode.SHIFT_NOT_FOUND);

            if (staff != null && shift != null && row.workDate() != null) {
                List<StaffShiftAssignment> comparable = assignmentsByStaffDate
                        .computeIfAbsent(key(staff.getId(), row.workDate()), ignored -> new ArrayList<>());
                errors.addAll(assignmentValidator.validate(staff, shift, row.workDate(), comparable));
                if (errors.isEmpty()) {
                    StaffShiftAssignment assignment = buildAssignment(staff, shift, row.workDate());
                    comparable.add(assignment);
                    preparedAssignments.add(assignment);
                }
            }

            responseRows.add(toResponse(row, staff, shift, List.copyOf(errors)));
        }

        int validCount = (int) responseRows.stream().filter(row -> "VALID".equals(row.getStatus())).count();
        StaffShiftImportPreviewResponse preview = StaffShiftImportPreviewResponse.builder()
                .summary(StaffShiftImportPreviewResponse.Summary.builder()
                        .totalRows(responseRows.size())
                        .validRows(validCount)
                        .invalidRows(responseRows.size() - validCount)
                        .build())
                .dateRange(StaffShiftImportPreviewResponse.DateRange.builder()
                        .startDate(startDate).endDate(endDate).build())
                .rows(responseRows)
                .build();
        return new ValidationResult(preview, preparedAssignments);
    }

    private ParsedRow parseRow(StaffShiftImportRowRequest row) {
        String email = value(row.getEmail());
        String dateValue = value(row.getWorkDate());
        String shiftCode = value(row.getShiftCode());
        Set<StaffShiftValidationCode> errors = new LinkedHashSet<>();
        LocalDate workDate = parseDate(dateValue);
        if (workDate == null) errors.add(StaffShiftValidationCode.INVALID_DATE);
        return new ParsedRow(row, normalizeEmail(email), normalizeShiftCode(shiftCode), workDate, List.copyOf(errors));
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next explicitly supported format.
            }
        }
        return null;
    }

    private StaffShiftAssignment buildAssignment(User staff, Shift shift, LocalDate workDate) {
        return StaffShiftAssignment.builder()
                .staff(staff)
                .shift(shift)
                .workDate(workDate)
                .position(StaffPosition.fromUserRole(UserRole.valueOf(staff.getRole().getName())))
                .build();
    }

    private StaffShiftImportRowResponse toResponse(
            ParsedRow row,
            User staff,
            Shift shift,
            List<StaffShiftValidationCode> errors
    ) {
        StaffShiftImportRowRequest source = row.source();
        UserShortResponse employee = staff == null ? null : userMapper.toUserShortResponse(staff);
        ShiftResponse resolvedShift = shift == null ? null : shiftMapper.toShiftResponse(shift);
        return StaffShiftImportRowResponse.builder()
                .rowNumber(source.getRowNumber())
                .email(value(source.getEmail()))
                .workDate(row.workDate() == null ? value(source.getWorkDate()) : row.workDate().toString())
                .shiftCode(row.normalizedShiftCode())
                .originalEmail(original(source.getOriginalEmail(), source.getEmail()))
                .originalWorkDate(original(source.getOriginalWorkDate(), source.getWorkDate()))
                .originalShiftCode(original(source.getOriginalShiftCode(), source.getShiftCode()))
                .employee(employee)
                .shift(resolvedShift)
                .status(errors.isEmpty() ? "VALID" : "INVALID")
                .validationErrors(errors)
                .build();
    }

    private String original(String original, String current) {
        return original == null ? value(current) : original;
    }

    private String value(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizeEmail(String value) {
        return value(value).toLowerCase(Locale.ROOT);
    }

    private String normalizeShiftCode(String value) {
        return value(value).toUpperCase(Locale.ROOT);
    }

    private String key(String staffId, LocalDate date) {
        return staffId + "|" + date;
    }

    private record ParsedRow(
            StaffShiftImportRowRequest source,
            String normalizedEmail,
            String normalizedShiftCode,
            LocalDate workDate,
            List<StaffShiftValidationCode> parsingErrors
    ) {}

    private record ValidationResult(
            StaffShiftImportPreviewResponse preview,
            List<StaffShiftAssignment> assignments
    ) {}
}
