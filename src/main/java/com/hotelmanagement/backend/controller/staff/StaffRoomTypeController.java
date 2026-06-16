package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.mapper.RoomTypeMapper;
import com.hotelmanagement.backend.service.RoomTypeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff/room-types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffRoomTypeController {
    RoomTypeService roomTypeService;
    RoomTypeMapper roomTypeMapper;

    @PreAuthorize("hasAuthority('ROOM_TYPE_CREATE')")
    @PostMapping("")
    public ApiResponse<RoomTypeResponse> createRoomType(@RequestBody @Valid RoomTypeCreationRequest request) {
        RoomTypeResponse roomTypeResponse = roomTypeService.createRoomType(request);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }

    @PreAuthorize("hasAuthority('ROOM_TYPE_READ')")
    @GetMapping("/{id}")
    public ApiResponse<RoomTypeResponse> getById(@PathVariable Long id) {
        RoomTypeResponse roomTypeResponse = roomTypeMapper.toRoomTypeResponse(roomTypeService.getRoomType(id));

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }

    @PreAuthorize("hasAuthority('ROOM_TYPE_READ')")
    @GetMapping("")
    public ApiResponse<List<RoomTypeResponse>> getList(
            @RequestParam (defaultValue = "1", required = false) int page,
            @RequestParam (defaultValue = "10", required = false) int limit,
            @RequestParam (defaultValue = "", required = false) String q) {
        PageRequest pageRequest = PageRequest.of(page -1 , limit);
        Page<RoomTypeResponse> response = roomTypeService.getRoomTypes(pageRequest, q);
        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<RoomTypeResponse>>builder().data(response.getContent()).pagination(meta).build();
    }

    @PreAuthorize("hasAuthority('ROOM_TYPE_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);

        return ApiResponse.<String>builder()
                .message("Delete Room Type Successfully")
                .build();
    }

    @PreAuthorize("hasAuthority('ROOM_TYPE_UPDATE')")
    @PutMapping("/{id}")
    public ApiResponse<RoomTypeResponse> updateRoomType(@RequestBody RoomTypeUpdateRequest request, @PathVariable Long id) {
        RoomTypeResponse roomTypeResponse = roomTypeService.updateRoomType(id, request);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }
}
