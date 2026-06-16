package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.response.AmenityResponse;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.mapper.AmenityMapper;
import com.hotelmanagement.backend.service.AmenityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/staff/amenities")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffAmenityController {
    AmenityMapper amenityMapper;
    AmenityService amenityService;
    @PreAuthorize("hasAuthority('AMENITY_READ')")
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
