package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.ReviewUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.ReviewResponse;
import com.hotelmanagement.backend.service.ReviewService;
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
@RequestMapping("/staff/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffReviewController {
    ReviewService reviewService;

    @PreAuthorize("hasAuthority('REVIEW_READ')")
    @GetMapping
    public ApiResponse<List<ReviewResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<ReviewResponse> response = reviewService.getList(pageRequest);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<ReviewResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @PreAuthorize("hasAuthority('REVIEW_UPDATE')")
    @PatchMapping("/{id}/active")
    public ApiResponse<ReviewResponse> updateActive(
            @PathVariable String id,
            @RequestBody @Valid ReviewUpdateRequest request
    ) {
        return ApiResponse.<ReviewResponse>builder()
                .data(reviewService.updateActive(id, request))
                .build();
    }

}
