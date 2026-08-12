package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.event.BookingConfirmedEvent;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.CancelReasonRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingConfirmationTransitionTest {
    @Mock BookingRepository bookingRepository;
    @Mock BookingMapper bookingMapper;
    @Mock UserService userService;
    @Mock InvoiceService invoiceService;
    @Mock RoomService roomService;
    @Mock InvoiceItemService invoiceItemService;
    @Mock PricingService pricingService;
    @Mock InvoicePromotionService invoicePromotionService;
    @Mock HousekeepingTaskService housekeepingTaskService;
    @Mock PromotionService promotionService;
    @Mock CancelReasonRepository cancelReasonRepository;
    @Mock ApplicationEventPublisher eventPublisher;

    @InjectMocks BookingService bookingService;

    @Test
    void publishesOneConfirmationEventOnlyForPendingToConfirmedTransition() {
        Invoice invoice = Invoice.builder()
                .status(InvoiceStatus.PENDING)
                .payments(Set.of(Payment.builder().status(PaymentStatus.SUCCESS).build()))
                .build();
        Booking booking = Booking.builder()
                .id("booking-id")
                .status(BookingStatus.PENDING)
                .invoice(invoice)
                .build();
        when(bookingRepository.findByIdForConfirmation("booking-id", BookingStatus.NO_SHOW))
                .thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        bookingService.confirmBooking("booking-id");
        bookingService.confirmBooking("booking-id");

        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals(InvoiceStatus.ACTIVE, invoice.getStatus());
        ArgumentCaptor<BookingConfirmedEvent> eventCaptor =
                ArgumentCaptor.forClass(BookingConfirmedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals("booking-id", eventCaptor.getValue().bookingId());
        verify(bookingRepository, times(1)).save(booking);
    }
}
