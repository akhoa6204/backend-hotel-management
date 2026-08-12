package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.config.AppProperties;
import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.InvoicePromotion;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.entity.RoomTypeImage;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.repository.BookingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingConfirmationEmailDataFactory {
    static Pattern UNSUITABLE_IMAGE_PATTERN = Pattern.compile(
            "(meme|joke|giphy|tenor|\\.gif(?:\\?|$))",
            Pattern.CASE_INSENSITIVE
    );

    BookingRepository bookingRepository;
    AppProperties appProperties;

    @Transactional(readOnly = true)
    public BookingConfirmationEmailData create(String bookingId) {
        Booking booking = bookingRepository.findConfirmationEmailDetailById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        Invoice invoice = booking.getInvoice();

        List<InvoiceItem> roomItems = invoice.getInvoiceItems().stream()
                .filter(item -> item.getType() == InvoiceItemType.ROOM)
                .toList();
        List<InvoiceItem> serviceItems = invoice.getInvoiceItems().stream()
                .filter(item -> item.getType() == InvoiceItemType.SERVICE)
                .toList();

        BigDecimal roomSubtotal = sumItems(roomItems);
        BigDecimal servicesSubtotal = sumItems(serviceItems);
        BigDecimal otherChargesSubtotal = invoice.getSubtotal()
                .subtract(roomSubtotal)
                .subtract(servicesSubtotal)
                .max(BigDecimal.ZERO);
        BigDecimal discountAmount = valueOrZero(invoice.getDiscountAmount());
        BigDecimal totalAmount = invoice.getSubtotal()
                .subtract(discountAmount)
                .max(BigDecimal.ZERO);

        List<Payment> successfulPayments = invoice.getPayments().stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .toList();
        Optional<Payment> confirmationPayment = successfulPayments.stream()
                .max(Comparator.comparing(
                        Payment::getPaidAt,
                        Comparator.nullsFirst(Comparator.naturalOrder())
                ));

        String recipientEmail = firstNonBlank(
                booking.getGuestEmail(),
                booking.getCustomer() == null ? null : booking.getCustomer().getEmail()
        );

        String imageUrl = booking.getRoom().getRoomType().getRoomTypeImages().stream()
                .filter(this::isSuitablePublicImage)
                .map(RoomTypeImage::getUrl)
                .findFirst()
                .orElse(null);
        String imageAlt = booking.getRoom().getRoomType().getRoomTypeImages().stream()
                .filter(image -> imageUrl != null && imageUrl.equals(image.getUrl()))
                .map(RoomTypeImage::getAlt)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(booking.getRoom().getRoomType().getName());

        String promotionName = invoice.getInvoicePromotions().stream()
                .map(InvoicePromotion::getPromotionName)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted()
                .reduce((first, second) -> first + ", " + second)
                .orElse(null);

        return BookingConfirmationEmailData.builder()
                .bookingCode(booking.getBookingCode())
                .locale(booking.getEmailLocale() == null
                        ? BookingEmailLocale.VI
                        : booking.getEmailLocale())
                .recipientEmail(recipientEmail)
                .guestName(booking.getGuestName())
                .guestPhone(booking.getGuestPhone())
                .roomName(booking.getRoom().getName())
                .roomTypeName(booking.getRoom().getRoomType().getName())
                .roomImageUrl(imageUrl)
                .roomImageAlt(imageAlt)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .numberOfNights(ChronoUnit.DAYS.between(
                        booking.getCheckInDate(),
                        booking.getCheckOutDate()
                ))
                .services(serviceItems.stream()
                        .filter(item -> item.getExtraService() != null)
                        .map(item -> BookingConfirmationEmailData.ServiceLine.builder()
                                .name(item.getExtraService().getName())
                                .quantity(item.getQuantity())
                                .amount(itemAmount(item))
                                .build())
                        .toList())
                .roomSubtotal(roomSubtotal)
                .servicesSubtotal(servicesSubtotal)
                .otherChargesSubtotal(otherChargesSubtotal)
                .discountAmount(discountAmount)
                .promotionName(promotionName)
                .totalAmount(totalAmount)
                .amountPaid(successfulPayments.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .paymentMethod(confirmationPayment.map(Payment::getMethod).orElse(null))
                .paymentStatus(confirmationPayment.map(Payment::getStatus).orElse(null))
                .bookingStatus(booking.getStatus())
                .bookingDetailUrl(buildBookingDetailUrl(booking))
                .build();
    }

    private BigDecimal sumItems(List<InvoiceItem> items) {
        return items.stream()
                .map(this::itemAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal itemAmount(InvoiceItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : null;
    }

    private boolean isSuitablePublicImage(RoomTypeImage image) {
        if (image.getUrl() == null || image.getUrl().isBlank()) {
            return false;
        }
        String candidate = image.getUrl() + " " + Optional.ofNullable(image.getAlt()).orElse("");
        if (UNSUITABLE_IMAGE_PATTERN.matcher(candidate).find()) {
            return false;
        }
        try {
            URI uri = URI.create(image.getUrl());
            return ("https".equalsIgnoreCase(uri.getScheme())
                    || "http".equalsIgnoreCase(uri.getScheme()))
                    && uri.getHost() != null
                    && !isLocalHost(uri.getHost());
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private String buildBookingDetailUrl(Booking booking) {
        if (booking.getCustomer() == null || booking.getCustomer().getId() == null) {
            return null;
        }
        String origin = appProperties.getOrigin();
        if (origin == null || origin.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(origin);
            if (uri.getHost() == null || isLocalHost(uri.getHost())) {
                return null;
            }
            return origin.replaceAll("/+$", "") + "/account/bookings/" + booking.getId();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private boolean isLocalHost(String host) {
        String normalized = host.toLowerCase(Locale.ROOT);
        return normalized.equals("localhost")
                || normalized.equals("127.0.0.1")
                || normalized.equals("::1");
    }
}
