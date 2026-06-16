package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.mapper.RoomMapper;
import com.hotelmanagement.backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/staff/rooms")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffRoomController {
    RoomService roomService;
    RoomMapper roomMapper;

    @PreAuthorize("hasAuthority('ROOM_CREATE')")
    @PostMapping("")
    public ApiResponse<RoomResponse> createRoom(@RequestBody @Valid RoomCreationRequest request) {
        RoomResponse roomResponse = roomMapper.toRoomResponse(roomService.createRoom(request));

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }

    @PreAuthorize("hasAuthority('ROOM_READ')")
    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getById(@PathVariable Long id) {
        RoomResponse roomResponse =roomMapper.toRoomResponse (roomService.getByid(id));

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }

    @PreAuthorize("hasAuthority('ROOM_READ')")
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
        ).map(roomMapper::toRoomResponse);

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

    @PreAuthorize("hasAuthority('ROOM_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRoom(@PathVariable Long id) {
        roomService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Room Successfully")
                .build();
    }

    @PreAuthorize("hasAuthority('ROOM_UPDATE')")
    @PatchMapping("/{id}")
    public ApiResponse<RoomResponse> updateRoom(@RequestBody RoomUpdateRequest request, @PathVariable Long id) {
        RoomResponse roomResponse = roomMapper.toRoomResponse(roomService.updateRoom(id, request));

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }
}
