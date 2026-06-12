package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.StaffShiftCreationRequest;
import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.AmenityMapper;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.service.AmenityService;
import com.hotelmanagement.backend.service.ShiftService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftController {
    ShiftService shiftService;
    ShiftMapper shiftMapper;
    @GetMapping
    public ApiResponse<List<StaffShiftResponse>> getSchedule(
            @RequestParam() LocalDate startDate,
            @RequestParam() LocalDate endDate,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) UserRole position
            ){
        SecurityContext context = SecurityContextHolder.getContext();
        String userId = Objects.requireNonNull(context.getAuthentication()).getName();

        List<StaffShiftResponse> staffShiftResponses = shiftService.getSchedule(q, startDate, endDate, position,  userId);
        return ApiResponse.<List<StaffShiftResponse>>builder()
                .data(staffShiftResponses)
                .build();
    }

    @GetMapping("/definitions")
    public ApiResponse<List<ShiftResponse>> getDefinitions(){
        List<ShiftResponse> shiftResponses = shiftService.findAllDefinition().stream().map(shiftMapper::toShiftResponse).toList();
        return ApiResponse.<List<ShiftResponse>>builder()
                .data(shiftResponses)
                .build();
    }

    @PostMapping("")
    public ApiResponse createStaffShift(@RequestBody @Valid StaffShiftCreationRequest request){
        shiftService.createStaffShift(request);
        return ApiResponse.builder()
                .message("Tạo lịch làm nhân viên thành công")
                .build();
    }
}
