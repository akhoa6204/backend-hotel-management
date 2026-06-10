package com.hotelmanagement.backend.scheduler;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.repository.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingScheduler {
    BookingRepository bookingRepository;
    @Scheduled(
            cron = "0 */3 * * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    @Transactional
    public void updateNoShowBookingsWithoutPayment(){
        LocalDateTime expiredAt = LocalDateTime.now().minusMinutes(3);
        List<Booking> bookings = bookingRepository.findNoPaidNoShowBookings(expiredAt);
        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.NO_SHOW);
        }
        bookingRepository.saveAll(bookings);
    }
    @Scheduled(
            cron = "0 */3 * * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    @Transactional
    public void updateNoShowBookingsWithPayment(){
        LocalDate today = LocalDate.now();
        List<Booking> bookings = bookingRepository.findPaidNoShowBookings(today);
        for (Booking booking : bookings) {
            booking.setStatus(BookingStatus.NO_SHOW);
        }
        bookingRepository.saveAll(bookings);
    }
}
