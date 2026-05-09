package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.RoomTypeCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomTypeUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.service.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/room-types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomTypeController {
    RoomTypeService roomTypeService;

    @PostMapping("")
    public ApiResponse<RoomTypeResponse> createRoomType(@RequestBody RoomTypeCreationRequest request) {
        RoomTypeResponse roomTypeResponse = roomTypeService.createRoomType(request);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<RoomTypeResponse> getById(@PathVariable Long id) {
        RoomTypeResponse roomTypeResponse = roomTypeService.getRoomType(id);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }

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

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRoomType(@PathVariable Long id) {
        roomTypeService.deleteRoomType(id);

        return ApiResponse.<String>builder()
                .message("Delete Room Type Successfully")
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<RoomTypeResponse> updateRoomType(@RequestBody RoomTypeUpdateRequest request, @PathVariable Long id) {
        RoomTypeResponse roomTypeResponse = roomTypeService.updateRoomType(id, request);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }
}
