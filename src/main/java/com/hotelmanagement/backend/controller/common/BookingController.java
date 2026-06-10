package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.response.PricingResultResponse;
import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.service.BookingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;
    BookingMapper bookingMapper;
    @PostMapping("")
    ApiResponse<BookingCreationResponse> create(@RequestBody @Valid BookingCreationRequest request) {
        BookingCreationResponse result = bookingService.create(request);

        return ApiResponse.<BookingCreationResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookingResponse> getById(@PathVariable String id) {
        BookingResponse result = bookingService.getByid(id);

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
    ApiResponse<BookingResponse> updateRoom(@PathVariable String id, @RequestBody BookingUpdateRequest request) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.updateBooking(id ,request));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/confirm")
    ApiResponse<BookingResponse> confirmBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.confirmBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/checkin")
    ApiResponse<BookingResponse> checkinBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.checkinBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/checkout")
    ApiResponse<BookingResponse> checkoutBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.checkoutBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PatchMapping("/{id}/cancelled")
    ApiResponse<BookingResponse> cancelledBooking(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.cancelBooking(id));
        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @PostMapping("/quote")
    ApiResponse<PricingResultResponse> quote(@RequestBody @Valid QuoteRequest request) {
        PricingResultResponse result = bookingService.quote(request);
        return ApiResponse.<PricingResultResponse>builder()
                .data(result)
                .build();
    }

}
