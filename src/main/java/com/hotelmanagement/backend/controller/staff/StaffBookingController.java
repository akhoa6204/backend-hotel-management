package com.hotelmanagement.backend.controller.staff;

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
@RequestMapping("/staff/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffBookingController {
    BookingService bookingService;
    BookingMapper bookingMapper;
    @PostMapping("")
    @PreAuthorize("hasAuthority('BOOKING_CREATE')")
    ApiResponse<BookingResponse> create(@RequestBody @Valid BookingCreationRequest request) {
        BookingResponse result = bookingService.create(request);

        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    ApiResponse<BookingResponse> getById(@PathVariable String id) {
        BookingResponse result = bookingService.getStaffBookingById(id);

        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    ApiResponse<List<BookingResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);

        Page<BookingResponse> response = bookingService.getList(pageRequest, q).map(bookingMapper::toBookingResponse);

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

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    ApiResponse<BookingResponse> updateRoom(@PathVariable String id, @RequestBody BookingUpdateRequest request) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.updateBooking(id ,request));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    ApiResponse<BookingResponse> confirmBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.confirmBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/checkin")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    ApiResponse<BookingResponse> checkinBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.checkinBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/checkout")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    ApiResponse<BookingResponse> checkoutBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.checkoutBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/cancelled")
    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    ApiResponse<BookingResponse> cancelledBooking(@PathVariable String id, @RequestBody @Valid BookingCancelRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.cancelBooking(userId, id, request));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/quote")
    @PreAuthorize("hasAuthority('BOOKING_READ')")
    ApiResponse<PricingResultResponse> quote(@RequestBody @Valid QuoteRequest request) {
        PricingResultResponse result = bookingService.quote(request);
        return ApiResponse.<PricingResultResponse>builder()
                .data(result)
                .build();
    }

}
