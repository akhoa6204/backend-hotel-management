package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.event.BookingConfirmedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingConfirmationEmailServiceTest {
    @Mock BookingConfirmationEmailDataFactory dataFactory;
    @Mock EmailService emailService;

    @InjectMocks BookingConfirmationEmailService confirmationEmailService;

    @Test
    void sendsRenderedEmailAfterConfirmation() {
        when(dataFactory.create("booking-id")).thenReturn(minimalData());

        confirmationEmailService.sendAfterBookingConfirmed(new BookingConfirmedEvent("booking-id"));

        verify(emailService).sendHtmlEmail(
                org.mockito.ArgumentMatchers.eq("guest@example.com"),
                org.mockito.ArgumentMatchers.contains("BOOK_1"),
                org.mockito.ArgumentMatchers.contains("Booking confirmed")
        );
    }

    @Test
    void mailFailureDoesNotEscapePostCommitHandler() {
        when(dataFactory.create("booking-id")).thenReturn(minimalData());
        doThrow(new RuntimeException("SMTP unavailable"))
                .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> confirmationEmailService
                .sendAfterBookingConfirmed(new BookingConfirmedEvent("booking-id")));
    }

    private BookingConfirmationEmailData minimalData() {
        return BookingConfirmationEmailData.builder()
                .bookingCode("BOOK_1")
                .locale(BookingEmailLocale.EN)
                .recipientEmail("guest@example.com")
                .guestName("Guest")
                .roomTypeName("STANDARD")
                .checkInDate(LocalDate.of(2026, 8, 15))
                .checkOutDate(LocalDate.of(2026, 8, 16))
                .numberOfNights(1)
                .services(List.of())
                .roomSubtotal(new BigDecimal("1000000"))
                .servicesSubtotal(BigDecimal.ZERO)
                .otherChargesSubtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(new BigDecimal("1000000"))
                .amountPaid(new BigDecimal("150000"))
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();
    }
}
