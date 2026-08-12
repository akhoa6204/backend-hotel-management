package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.event.BookingConfirmedEvent;
import com.hotelmanagement.backend.template.BookingConfirmationEmailTemplate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingConfirmationEmailService {
    BookingConfirmationEmailDataFactory dataFactory;
    EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendAfterBookingConfirmed(BookingConfirmedEvent event) {
        try {
            BookingConfirmationEmailData data = dataFactory.create(event.bookingId());
            if (data.getRecipientEmail() == null || data.getRecipientEmail().isBlank()) {
                log.warn("Booking confirmation email skipped because no recipient is available for booking {}",
                        event.bookingId());
                return;
            }

            BookingConfirmationEmailTemplate.RenderedEmail rendered =
                    BookingConfirmationEmailTemplate.render(data);
            emailService.sendHtmlEmail(
                    data.getRecipientEmail(),
                    rendered.subject(),
                    rendered.html()
            );
        } catch (Exception exception) {
            log.error("Booking {} was confirmed, but its confirmation email could not be sent",
                    event.bookingId(), exception);
        }
    }
}
