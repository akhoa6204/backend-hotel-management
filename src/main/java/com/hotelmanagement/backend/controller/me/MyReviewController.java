package com.hotelmanagement.backend.controller.me;

import com.hotelmanagement.backend.dto.request.ReviewCreationRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/me/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyReviewController {
    ReviewService reviewService;

    @PreAuthorize("hasAuthority('REVIEW_CREATE')")
    @PostMapping("/bookings/{bookingId}")
    ApiResponse<ReviewResponse> create(
            @PathVariable String bookingId,
            @RequestBody @Valid ReviewCreationRequest request
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return ApiResponse.<ReviewResponse>builder()
                .data(reviewService.createMyReview(userId, bookingId, request))
                .build();
    }

    @PreAuthorize("hasAuthority('REVIEW_READ')")
    @GetMapping
    ApiResponse<List<ReviewResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        Page<ReviewResponse> response = reviewService.getMyReviews(userId, pageRequest);

        MetaPagination metaPagination = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<ReviewResponse>>builder()
                .data(response.getContent())
                .pagination(metaPagination)
                .build();
    }

    @PreAuthorize("hasAuthority('REVIEW_READ')")
    @GetMapping("/{id}")
    ApiResponse<ReviewResponse> getById(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        return ApiResponse.<ReviewResponse>builder()
                .data(reviewService.getMyReviewById(userId, id))
                .build();
    }
}
