package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.service.RoomService;
import com.hotelmanagement.backend.service.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/rooms")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {
    RoomService roomService;

    @PostMapping("")
    public ApiResponse<RoomResponse> createRoom(@RequestBody RoomCreationRequest request) {
        RoomResponse roomResponse = roomService.createRoom(request);

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getById(@PathVariable Long id) {
        RoomResponse roomResponse = roomService.getByid(id);

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }

    @GetMapping
    public ApiResponse<List<RoomResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @RequestParam(required = false)
            Long roomTypeId
    ) {

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                limit
        );

        Page<RoomResponse> response = roomService.getList(
                pageRequest,
                q,
                startDate,
                endDate,
                roomTypeId
        );

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<RoomResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRoomType(@PathVariable Long id) {
        roomService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Room Successfully")
                .build();
    }


    @PutMapping("/{id}")
    public ApiResponse<RoomResponse> updateRoomType(@RequestBody RoomUpdateRequest request, @PathVariable Long id) {
        RoomResponse roomResponse = roomService.updateRoom(id, request);

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }
}
