package com.hotelmanagement.backend.dto.internal;

import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Value
@Builder
public class BookingConfirmationEmailData {
    String bookingCode;
    BookingEmailLocale locale;
    String recipientEmail;
    String guestName;
    String guestPhone;
    String roomName;
    String roomTypeName;
    String roomImageUrl;
    String roomImageAlt;
    LocalDate checkInDate;
    LocalDate checkOutDate;
    long numberOfNights;
    List<ServiceLine> services;
    BigDecimal roomSubtotal;
    BigDecimal servicesSubtotal;
    BigDecimal otherChargesSubtotal;
    BigDecimal discountAmount;
    String promotionName;
    BigDecimal totalAmount;
    BigDecimal amountPaid;
    PaymentMethod paymentMethod;
    PaymentStatus paymentStatus;
    BookingStatus bookingStatus;
    String bookingDetailUrl;

    @Value
    @Builder
    public static class ServiceLine {
        String name;
        long quantity;
        BigDecimal amount;
    }
}
