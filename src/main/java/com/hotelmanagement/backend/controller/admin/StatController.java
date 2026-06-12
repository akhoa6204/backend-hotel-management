package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.service.StatService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatController {

    StatService statService;

    @GetMapping("/overview")
    public ApiResponse<StatsOverviewResponse> getOverview() {
        return ApiResponse.<StatsOverviewResponse>builder()
                .data(statService.getOverview())
                .build();
    }

    @GetMapping("/checkins")
    public ApiResponse<List<BookingResponse>> getCheckins(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page -1 , limit);
        Page<BookingResponse> bookingResponsePage = statService.getCheckins(pageRequest);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(bookingResponsePage.hasPrevious())
                .hasNext(bookingResponsePage.hasNext())
                .limit(bookingResponsePage.getSize())
                .page(page)
                .total(bookingResponsePage.getTotalElements())
                .totalPages(bookingResponsePage.getTotalPages())
                .build();
        return ApiResponse.<List<BookingResponse>>builder()
                .data(bookingResponsePage.getContent())
                .pagination(meta)
                .build();
    }

    @GetMapping("/checkouts")
    public ApiResponse<List<BookingResponse>> getCheckouts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit
    ) {
        PageRequest pageRequest = PageRequest.of(page - 1, limit);
        Page<BookingResponse> bookingResponsePage = statService.getCheckouts(pageRequest);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(bookingResponsePage.hasPrevious())
                .hasNext(bookingResponsePage.hasNext())
                .limit(bookingResponsePage.getSize())
                .page(page)
                .total(bookingResponsePage.getTotalElements())
                .totalPages(bookingResponsePage.getTotalPages())
                .build();
        return ApiResponse.<List<BookingResponse>>builder()
                .data(bookingResponsePage.getContent())
                .pagination(meta)
                .build();
    }

    @GetMapping("/occupancy")
    public ApiResponse<RevenueOccupancyStatsResponse> getOccupancy() {
        return ApiResponse.<RevenueOccupancyStatsResponse>builder()
                .data(statService.getOccupancy())
                .build();
    }

    @GetMapping("/revenue")
    public ApiResponse<MonthlyRevenueResponse> getRevenue() {
        return ApiResponse.<MonthlyRevenueResponse>builder()
                .data(statService.getRevenue())
                .build();
    }

    @GetMapping("/bookings")
    public ApiResponse<MonthlyBookingStatsResponse> getBookingStats(
            @RequestParam(required = false) String month
    ) {
        return ApiResponse.<MonthlyBookingStatsResponse>builder()
                .data(statService.getBookingStats(month))
                .build();
    }
}
