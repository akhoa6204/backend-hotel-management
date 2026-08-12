package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class BookingConfirmationEmailPreviewTest {

    @Test
    void writesVietnameseAndEnglishDeveloperPreviews() throws IOException {
        Path previewDirectory = Path.of("build", "email-previews");
        Files.createDirectories(previewDirectory);

        writePreview(previewDirectory.resolve("booking-confirmation-vi.html"), BookingEmailLocale.VI);
        writePreview(previewDirectory.resolve("booking-confirmation-en.html"), BookingEmailLocale.EN);
    }

    private void writePreview(Path destination, BookingEmailLocale locale) throws IOException {
        BookingConfirmationEmailData base =
                BookingConfirmationEmailTemplateTest.emailData(locale, true, false);
        BookingConfirmationEmailData preview = BookingConfirmationEmailData.builder()
                .bookingCode(base.getBookingCode())
                .locale(base.getLocale())
                .recipientEmail(base.getRecipientEmail())
                .guestName(base.getGuestName())
                .guestPhone(base.getGuestPhone())
                .roomName(base.getRoomName())
                .roomTypeName(base.getRoomTypeName())
                .roomImageUrl(Path.of("../FE/src/assets/images/room-1.jpg")
                        .toAbsolutePath().normalize().toUri().toString())
                .roomImageAlt(base.getRoomImageAlt())
                .checkInDate(base.getCheckInDate())
                .checkOutDate(base.getCheckOutDate())
                .numberOfNights(base.getNumberOfNights())
                .services(base.getServices())
                .roomSubtotal(base.getRoomSubtotal())
                .servicesSubtotal(base.getServicesSubtotal())
                .otherChargesSubtotal(base.getOtherChargesSubtotal())
                .discountAmount(base.getDiscountAmount())
                .promotionName(base.getPromotionName())
                .totalAmount(base.getTotalAmount())
                .amountPaid(base.getAmountPaid())
                .paymentMethod(base.getPaymentMethod())
                .paymentStatus(base.getPaymentStatus())
                .bookingStatus(base.getBookingStatus())
                .bookingDetailUrl(base.getBookingDetailUrl())
                .build();

        Files.writeString(destination, BookingConfirmationEmailTemplate.render(preview).html());
    }
}
