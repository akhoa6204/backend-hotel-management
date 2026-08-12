package com.hotelmanagement.backend.template;

import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingConfirmationEmailTemplateTest {

    @ParameterizedTest
    @EnumSource(BookingEmailLocale.class)
    void rendersLocalizedCustomerSafeEmail(BookingEmailLocale locale) {
        BookingConfirmationEmailTemplate.RenderedEmail rendered =
                BookingConfirmationEmailTemplate.render(emailData(locale, true, true));

        assertTrue(rendered.subject().contains("BOOK_20260810"));
        assertTrue(rendered.html().contains("https://res.cloudinary.com/diamond-sea/room.jpg"));
        assertTrue(rendered.html().contains("Breakfast &amp; &lt;script&gt;"));
        assertTrue(rendered.html().contains("https://diamondsea.example/account/bookings/booking-id"));
        assertFalse(rendered.html().contains("<script>alert"));
        assertFalse(rendered.html().contains("payment.status.SUCCESS"));
        assertFalse(rendered.html().contains("bookingEmail."));
        assertFalse(rendered.html().contains("localhost"));
        assertFalse(rendered.html().contains("undefined"));
        assertFalse(rendered.html().contains("[object Object]"));
        assertFalse(rendered.html().contains(">CONFIRMED<"));
        assertFalse(rendered.html().contains("DA NANG"));
        assertFalse(rendered.html().contains("DỊCH VỤ BỔ SUNG"));
        assertFalse(rendered.html().contains("ADDITIONAL SERVICES"));

        if (locale == BookingEmailLocale.EN) {
            assertTrue(rendered.html().contains("Booking confirmed"));
            assertTrue(rendered.html().contains("Bank transfer"));
        } else {
            assertTrue(rendered.html().contains("Đặt phòng thành công"));
            assertTrue(rendered.html().contains("Chuyển khoản ngân hàng"));
        }
    }

    @Test
    void omitsOptionalImageServicesPromotionAndCta() {
        String html = BookingConfirmationEmailTemplate
                .render(emailData(BookingEmailLocale.VI, false, false))
                .html();

        assertFalse(html.contains("<img"));
        assertFalse(html.contains("Dịch vụ bổ sung"));
        assertFalse(html.contains("Ưu đãi ·"));
        assertFalse(html.contains("Xem đặt phòng"));
    }

    static BookingConfirmationEmailData emailData(
            BookingEmailLocale locale,
            boolean withOptionals,
            boolean withImage
    ) {
        return BookingConfirmationEmailData.builder()
                .bookingCode("BOOK_20260810")
                .locale(locale)
                .recipientEmail("guest@example.com")
                .guestName("Nguyễn <script>alert(1)</script> Văn A with a long guest name")
                .guestPhone("0901234567")
                .roomName("Room 501")
                .roomTypeName("Premium Ocean Room with an Extended Descriptive Name")
                .roomImageUrl(withImage
                        ? "https://res.cloudinary.com/diamond-sea/room.jpg"
                        : null)
                .roomImageAlt("Premium room")
                .checkInDate(LocalDate.of(2026, 8, 15))
                .checkOutDate(LocalDate.of(2026, 8, 18))
                .numberOfNights(3)
                .services(withOptionals
                        ? List.of(BookingConfirmationEmailData.ServiceLine.builder()
                                .name("Breakfast & <script>")
                                .quantity(2)
                                .amount(new BigDecimal("300000"))
                                .build())
                        : List.of())
                .roomSubtotal(new BigDecimal("3000000"))
                .servicesSubtotal(withOptionals ? new BigDecimal("300000") : BigDecimal.ZERO)
                .otherChargesSubtotal(BigDecimal.ZERO)
                .discountAmount(withOptionals ? new BigDecimal("300000") : BigDecimal.ZERO)
                .promotionName(withOptionals ? "Summer <Deal>" : null)
                .totalAmount(new BigDecimal("3000000"))
                .amountPaid(new BigDecimal("150000"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .paymentStatus(PaymentStatus.SUCCESS)
                .bookingStatus(BookingStatus.CONFIRMED)
                .bookingDetailUrl(withOptionals
                        ? "https://diamondsea.example/account/bookings/booking-id"
                        : null)
                .build();
    }
}
