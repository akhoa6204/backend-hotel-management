package com.hotelmanagement.backend.controller.me;

import com.hotelmanagement.backend.dto.request.BookingCancelRequest;
import com.hotelmanagement.backend.dto.request.BookingCreationRequest;
import com.hotelmanagement.backend.dto.request.BookingUpdateRequest;
import com.hotelmanagement.backend.dto.request.QuoteRequest;
import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MyBookingController {
    BookingService bookingService;
    BookingMapper bookingMapper;

    @GetMapping("/{id}")
    ApiResponse<BookingResponse> getById(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        BookingResponse result = bookingService.getMyBookingById(userId, id);

        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("")
    ApiResponse<List<BookingResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q
    ) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        Page<BookingResponse> response = bookingService.getMyList(userId, pageRequest, q);

        MetaPagination metaPagination = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<BookingResponse>>builder()
                .data(response.getContent())
                .pagination(metaPagination)
                .build();
    }


    @PatchMapping("/{id}/cancelled")
    ApiResponse<BookingResponse> cancelledBooking(@PathVariable String id, @RequestBody @Valid BookingCancelRequest bookingCancelRequest) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        BookingResponse result = bookingMapper.toBookingResponse(bookingService.cancelMyBooking(userId, id, bookingCancelRequest));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }


}
