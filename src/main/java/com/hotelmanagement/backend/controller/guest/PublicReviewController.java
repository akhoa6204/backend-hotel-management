package com.hotelmanagement.backend.controller.guest;

import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.ReviewOverviewResponse;
import com.hotelmanagement.backend.dto.response.ReviewResponse;
import com.hotelmanagement.backend.service.ReviewService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/public/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicReviewController {
    ReviewService reviewService;

    @GetMapping("/room-types/{roomTypeId}")
    ApiResponse<List<ReviewResponse>> getReviewsByRoomType(
            @PathVariable String roomTypeId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<ReviewResponse> response = reviewService.getPublicReviewsByRoomType(roomTypeId, pageRequest);
        MetaPagination metaPagination = MetaPagination.builder()
                .page(page)
                .limit(limit)
                .total(response.getTotalElements())
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<ReviewResponse>>builder()
                .data(response.getContent())
                .pagination(metaPagination)
                .build();
    }

    @GetMapping("/room-types/{roomTypeId}/overview")
    ApiResponse<ReviewOverviewResponse> getOverviewReviewsByRoomType(
            @PathVariable String roomTypeId
    ) {
        ReviewOverviewResponse result = reviewService.getPublicReviewOverviewByRoomType(roomTypeId);

        return ApiResponse.<ReviewOverviewResponse>builder()
                .data(result)
                .build();
    }
}
