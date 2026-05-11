package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.service.AuthenticationService;
import com.hotelmanagement.backend.service.BookingService;
import com.hotelmanagement.backend.service.UserService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {
    BookingService bookingService;
    BookingMapper bookingMapper;
    @PostMapping("")
    ApiResponse<BookingResponse> create(@RequestBody BookingCreationRequest request) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.create(request));

        return ApiResponse.<BookingResponse>builder()
                .data(result)
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<BookingResponse> getById(@PathVariable String id) {
        BookingResponse result = bookingMapper.toBookingResponse(bookingService.getByid(id));

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
}
