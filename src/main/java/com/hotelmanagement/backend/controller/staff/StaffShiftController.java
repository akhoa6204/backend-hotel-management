package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.StaffShiftCreationRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.ShiftResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftResponse;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.service.ShiftService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/staff/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffShiftController {
    ShiftService shiftService;
    ShiftMapper shiftMapper;

    @PreAuthorize("hasAuthority('SHIFT_READ')")
    @GetMapping("/me")
    public ApiResponse<List<StaffShiftResponse>> getMySchedule(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        List<StaffShiftResponse> staffShiftResponses = shiftService.getMySchedule(startDate, endDate);
        return ApiResponse.<List<StaffShiftResponse>>builder()
                .data(staffShiftResponses)
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping
    public ApiResponse<List<StaffShiftResponse>> getSchedule(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) UserRole position
    ) {
        List<StaffShiftResponse> staffShiftResponses = shiftService.getSchedule(q, startDate, endDate, position);
        return ApiResponse.<List<StaffShiftResponse>>builder()
                .data(staffShiftResponses)
                .build();
    }

    @PreAuthorize("hasAuthority('SHIFT_READ')")
    @GetMapping("/definitions")
    public ApiResponse<List<ShiftResponse>> getDefinitions(){
        List<ShiftResponse> shiftResponses = shiftService.findAllDefinition().stream().map(shiftMapper::toShiftResponse).toList();
        return ApiResponse.<List<ShiftResponse>>builder()
                .data(shiftResponses)
                .build();
    }

    @PreAuthorize("hasAuthority('SHIFT_CREATE')")
    @PostMapping("")
    public ApiResponse createStaffShift(@RequestBody @Valid StaffShiftCreationRequest request){
        shiftService.createStaffShift(request);
        return ApiResponse.builder()
                .message("Tạo lịch làm nhân viên thành công")
                .build();
    }

    @PreAuthorize("hasAuthority('SHIFT_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteStaffShift(@PathVariable Integer id) {
        shiftService.deleteStaffShift(id);
        return ApiResponse.<Void>builder()
                .message("Xóa lịch làm nhân viên thành công")
                .build();
    }

}
