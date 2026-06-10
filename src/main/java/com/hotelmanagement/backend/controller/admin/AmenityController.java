package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.mapper.AmenityMapper;
import com.hotelmanagement.backend.service.AmenityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/amenities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AmenityController {
    AmenityMapper amenityMapper;
    AmenityService amenityService;
    @GetMapping
    public ApiResponse<List<AmenityResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int limit){
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<AmenityResponse> response = amenityService.getAmenities(pageRequest)
                .map(amenityMapper::toAmenityResponse);
        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<AmenityResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }
}
