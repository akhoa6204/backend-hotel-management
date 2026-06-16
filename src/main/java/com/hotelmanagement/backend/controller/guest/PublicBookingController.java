package com.hotelmanagement.backend.controller.guest;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/public/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicBookingController {
    BookingService bookingService;
    BookingMapper bookingMapper;
    @PostMapping("")
    ApiResponse<BookingResponse> create(@RequestBody @Valid BookingCreationRequest request) {
        BookingResponse result = bookingService.create(request);

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
