package com.hotelmanagement.backend.controller.guest;

import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomTypeResponse;
import com.hotelmanagement.backend.mapper.RoomTypeMapper;
import com.hotelmanagement.backend.service.RoomTypeService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/public/room-types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicRoomTypeController {
    RoomTypeService roomTypeService;
    RoomTypeMapper roomTypeMapper;

    @GetMapping("/{id}")
    public ApiResponse<RoomTypeResponse> getById(@PathVariable Long id) {
        RoomTypeResponse roomTypeResponse = roomTypeService.getPublicRoomType(id);

        return ApiResponse.<RoomTypeResponse>builder().data(roomTypeResponse).build();
    }

    @GetMapping("")
    public ApiResponse<List<RoomTypeResponse>> getList(
            @RequestParam (defaultValue = "1", required = false) int page,
            @RequestParam (defaultValue = "10", required = false) int limit,
            @RequestParam (defaultValue = "", required = false) String q,
            @RequestParam (required = false) LocalDate startDate,
            @RequestParam (required = false) LocalDate endDate,
            @RequestParam (required = false) Integer capacity,
            @RequestParam(defaultValue = "basePrice", required = false) String sortBy,
            @RequestParam(defaultValue = "asc", required = false) String sortOrder
            ) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        PageRequest pageRequest = PageRequest.of(page - 1 , limit, sort);
        Page<RoomTypeResponse> response = roomTypeService.getPublicRoomTypes(pageRequest, q, startDate, endDate, capacity);
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
}
