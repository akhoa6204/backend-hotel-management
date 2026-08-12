package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.StaffShiftCreationRequest;
import com.hotelmanagement.backend.dto.request.StaffShiftImportConfirmRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.ShiftResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftImportPreviewResponse;
import com.hotelmanagement.backend.dto.response.StaffShiftImportResultResponse;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.ShiftMapper;
import com.hotelmanagement.backend.service.ShiftService;
import com.hotelmanagement.backend.service.StaffShiftImportService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/staff/shifts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffShiftController {
    ShiftService shiftService;
    StaffShiftImportService staffShiftImportService;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(required = false) UserRole position,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Page<StaffShiftResponse> staffShiftResponses = shiftService.getSchedule(
                page, limit, q, startDate, endDate, position, sort
        );
        MetaPagination pagination = MetaPagination.builder()
                .page(staffShiftResponses.getNumber())
                .limit(staffShiftResponses.getSize())
                .total(staffShiftResponses.getTotalElements())
                .totalPages(staffShiftResponses.getTotalPages())
                .hasNext(staffShiftResponses.hasNext())
                .hasPrev(staffShiftResponses.hasPrevious())
                .build();

        return ApiResponse.<List<StaffShiftResponse>>builder()
                .data(staffShiftResponses.getContent())
                .pagination(pagination)
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

    @PreAuthorize("hasAuthority('SHIFT_CREATE')")
    @PostMapping(value = "/import/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StaffShiftImportPreviewResponse> previewImport(
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.<StaffShiftImportPreviewResponse>builder()
                .data(staffShiftImportService.preview(file))
                .build();
    }

    @PreAuthorize("hasAuthority('SHIFT_CREATE')")
    @PostMapping(value = "/import/preview", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<StaffShiftImportPreviewResponse> revalidateImport(
            @RequestBody StaffShiftImportConfirmRequest request
    ) {
        return ApiResponse.<StaffShiftImportPreviewResponse>builder()
                .data(staffShiftImportService.revalidate(request))
                .build();
    }

    @PreAuthorize("hasAuthority('SHIFT_CREATE')")
    @PostMapping(value = "/import/confirm", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<StaffShiftImportResultResponse> confirmImport(
            @RequestBody StaffShiftImportConfirmRequest request
    ) {
        StaffShiftImportResultResponse result = staffShiftImportService.confirm(request);
        return ApiResponse.<StaffShiftImportResultResponse>builder()
                .data(result)
                .message("Nhập lịch làm nhân viên thành công")
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
