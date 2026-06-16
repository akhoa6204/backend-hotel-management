package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.HousekeepingTaskCreationRequest;
import com.hotelmanagement.backend.dto.request.HousekeepingTaskUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.HousekeepingTaskResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.enums.HousekeepingTaskStatus;
import com.hotelmanagement.backend.mapper.HousekeepingTaskMapper;
import com.hotelmanagement.backend.service.HousekeepingTaskService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff/housekeepings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffHousekeepingTaskController {
    HousekeepingTaskService housekeepingTaskService;
    HousekeepingTaskMapper housekeepingTaskMapper;

    @PreAuthorize("hasAuthority('HOUSEKEEPING_TASK_READ')")
    @GetMapping("")
    public ApiResponse<List<HousekeepingTaskResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false)
            HousekeepingTaskStatus status,
            @RequestParam(required = false)
            String bookingId
    ) {

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                limit
        );

        Page<HousekeepingTaskResponse> response = housekeepingTaskService.getTaskList(pageRequest, status, q, bookingId).map(housekeepingTaskMapper::toHousekeepingTaskResponse);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<HousekeepingTaskResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @PreAuthorize("hasAuthority('HOUSEKEEPING_TASK_READ')")
    @GetMapping("/me")
    public ApiResponse<List<HousekeepingTaskResponse>> getMyHousekeepingTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) HousekeepingTaskStatus status,
            @RequestParam(required = false) String bookingId
    ) {
        SecurityContext context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        Page<HousekeepingTaskResponse> response = housekeepingTaskService
                .getMyTaskList(pageRequest, status, q, bookingId, userId)
                .map(housekeepingTaskMapper::toHousekeepingTaskResponse);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<HousekeepingTaskResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @PreAuthorize("hasAuthority('HOUSEKEEPING_TASK_READ')")
    @GetMapping("/{id}")
    public ApiResponse<HousekeepingTaskResponse> delete(@PathVariable Long id) {
        HousekeepingTaskResponse response = housekeepingTaskMapper.toHousekeepingTaskResponse(housekeepingTaskService.getByTaskId(id));
        return ApiResponse.<HousekeepingTaskResponse>builder()
                .data(response)
                .build();
    }


    @PreAuthorize("hasAuthority('HOUSEKEEPING_TASK_UPDATE')")
    @PutMapping("/{id}")
    public ApiResponse<HousekeepingTaskResponse> updateTask(@RequestBody HousekeepingTaskUpdateRequest request, @PathVariable Long id) {
        SecurityContext context = SecurityContextHolder.getContext();
        String userId = context.getAuthentication().getName();

        HousekeepingTaskResponse response = housekeepingTaskMapper
                .toHousekeepingTaskResponse(
                        housekeepingTaskService.updateTask(id ,userId, request));

        return ApiResponse.<HousekeepingTaskResponse>builder().data(response).build();
    }

    @PreAuthorize("hasAuthority('HOUSEKEEPING_TASK_CREATE')")
    @PostMapping("")
    public ApiResponse<HousekeepingTaskResponse> createTask(@RequestBody @Valid HousekeepingTaskCreationRequest request) {
        HousekeepingTaskResponse response = housekeepingTaskMapper.toHousekeepingTaskResponse(housekeepingTaskService.createTask(request));

        return ApiResponse.<HousekeepingTaskResponse>builder().data(response).build();
    }
}
