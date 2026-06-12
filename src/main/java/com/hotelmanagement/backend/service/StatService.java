package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.response.BookingResponse;
import com.hotelmanagement.backend.dto.response.MonthlyBookingStatsResponse;
import com.hotelmanagement.backend.dto.response.MonthlyRevenueItemResponse;
import com.hotelmanagement.backend.dto.response.MonthlyRevenueResponse;
import com.hotelmanagement.backend.dto.response.RevenueOccupancyStatsResponse;
import com.hotelmanagement.backend.dto.response.StatsOverviewResponse;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoiceRepository;
import com.hotelmanagement.backend.repository.RoomRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatService {

    BookingRepository bookingRepository;
    RoomRepository roomRepository;
    InvoiceRepository invoiceRepository;
    UserRepository userRepository;
    BookingMapper bookingMapper;
    public StatsOverviewResponse getOverview() {
        LocalDate today = LocalDate.now();

        LocalDateTime weekStart =
                today.with(DayOfWeek.MONDAY).atStartOfDay();

        LocalDateTime weekEnd =
                today.with(DayOfWeek.SUNDAY).atTime(23, 59, 59);

        long todayBookings =
                bookingRepository.countTodayBookings(today);

        long totalRooms =
                roomRepository.count();

        long availableRooms =
                roomRepository.countAvailableRoomsBetween(
                        today,
                        today
                );

        BigDecimal weekRevenue =
                invoiceRepository.getRevenueBetween(
                        weekStart,
                        weekEnd
                );

        Date monthStart = Date.from(
                today.withDayOfMonth(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        Date tomorrowStart = Date.from(
                today.plusDays(1)
                        .atStartOfDay(ZoneId.systemDefault())
                        .toInstant()
        );

        long newCustomers =
                userRepository.countNewCustomers(
                        "USER",
                        monthStart,
                        tomorrowStart
                );

        int occupancyPct = totalRooms == 0
                ? 0
                : (int) Math.round(
                ((double) (totalRooms - availableRooms)
                        / totalRooms) * 100
        );

        return StatsOverviewResponse.builder()
                .todayBookings((int) todayBookings)
                .totalRooms((int) totalRooms)
                .availableRooms((int) availableRooms)
                .occupancyPct(occupancyPct)
                .weekRevenue(weekRevenue)
                .newCustomers((int) newCustomers)
                .build();
    }

    public Page<BookingResponse> getCheckins(Pageable pageable) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByCheckInDate(today, pageable)
                .map(bookingMapper::toBookingResponse);
    }

    public Page<BookingResponse> getCheckouts(Pageable pageable) {
        LocalDate today = LocalDate.now();

        return bookingRepository.findByCheckOutDate(today, pageable)
                .map(bookingMapper::toBookingResponse);
    }

    public RevenueOccupancyStatsResponse getOccupancy() {
        LocalDate start =
                YearMonth.now().atDay(1);

        LocalDate end =
                YearMonth.now().atEndOfMonth();

        long totalRooms =
                roomRepository.count();

        long occupiedRooms =
                bookingRepository.countOccupiedRoomsBetween(
                        start,
                        end
                );

        int occupancyPct = totalRooms == 0
                ? 0
                : (int) Math.round(
                ((double) occupiedRooms / totalRooms) * 100
        );

        BigDecimal totalRevenue =
                invoiceRepository.getRevenueBetween(
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                );

        return RevenueOccupancyStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalRevenueDeltaPct(0D)
                .occupancyPct(occupancyPct)
                .occupancyDeltaPct(0D)
                .build();
    }

    public MonthlyRevenueResponse getRevenue() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(5);

        List<MonthlyRevenueItemResponse> months = java.util.stream.IntStream
                .rangeClosed(0, 5)
                .mapToObj(index -> {
                    YearMonth month = startMonth.plusMonths(index);

                    BigDecimal revenue = invoiceRepository.getRevenueBetween(
                            month.atDay(1).atStartOfDay(),
                            month.atEndOfMonth().atTime(23, 59, 59)
                    );

                    return MonthlyRevenueItemResponse.builder()
                            .month(month.toString())
                            .label("T" + month.getMonthValue())
                            .revenue(revenue)
                            .build();
                })
                .toList();

        return MonthlyRevenueResponse.builder()
                .months(months)
                .build();
    }

    public MonthlyBookingStatsResponse getBookingStats(String month) {
        YearMonth baseMonth = month == null || month.isBlank()
                ? YearMonth.now()
                : YearMonth.parse(month);

        LocalDate start = baseMonth.atDay(1);
        LocalDate end = baseMonth.plusMonths(1).atDay(1);

        List<BookingStatus> successStatuses = List.of(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN,
                BookingStatus.CHECKED_OUT
        );

        long total = bookingRepository
                .countByCheckInDateGreaterThanEqualAndCheckInDateLessThan(
                        start,
                        end
                );

        long success = bookingRepository
                .countByCheckInDateGreaterThanEqualAndCheckInDateLessThanAndStatusIn(
                        start,
                        end,
                        successStatuses
                );

        long cancelled = bookingRepository
                .countByCheckInDateGreaterThanEqualAndCheckInDateLessThanAndStatus(
                        start,
                        end,
                        BookingStatus.CANCELLED
                );

        int cancelRate = total == 0
                ? 0
                : (int) Math.round((double) cancelled * 100 / total);

        return MonthlyBookingStatsResponse.builder()
                .month(baseMonth.toString())
                .total((int) total)
                .success((int) success)
                .cancelled((int) cancelled)
                .cancelRate(cancelRate)
                .build();
    }
}