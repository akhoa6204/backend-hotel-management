package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.config.AppProperties;
import com.hotelmanagement.backend.dto.internal.BookingConfirmationEmailData;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.InvoicePromotion;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.RoomType;
import com.hotelmanagement.backend.entity.RoomTypeImage;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.BookingEmailLocale;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.PaymentMethod;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingConfirmationEmailDataFactoryTest {
    @Mock
    BookingRepository bookingRepository;

    BookingConfirmationEmailDataFactory dataFactory;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setOrigin("https://diamondsea.example/");
        dataFactory = new BookingConfirmationEmailDataFactory(bookingRepository, appProperties);
    }

    @ParameterizedTest
    @ValueSource(strings = {"STANDARD", "PREMIUM", "DELUXE"})
    void selectsTheBookedRoomTypesFirstSuitablePublicImage(String roomTypeName) {
        String expectedImage = "https://res.cloudinary.com/diamond-sea/" + roomTypeName + ".jpg";
        Set<RoomTypeImage> images = new LinkedHashSet<>();
        images.add(RoomTypeImage.builder()
                .url("https://media.example/meme.gif")
                .alt("Meme")
                .build());
        images.add(RoomTypeImage.builder()
                .url(expectedImage)
                .alt(roomTypeName + " room")
                .build());
        Booking booking = booking(roomTypeName, images);
        when(bookingRepository.findConfirmationEmailDetailById("booking-id"))
                .thenReturn(Optional.of(booking));

        BookingConfirmationEmailData data = dataFactory.create("booking-id");

        assertEquals(roomTypeName, data.getRoomTypeName());
        assertEquals(expectedImage, data.getRoomImageUrl());
        assertEquals("https://diamondsea.example/account/bookings/booking-id",
                data.getBookingDetailUrl());
        assertEquals(new BigDecimal("300000"), data.getServicesSubtotal());
        assertEquals(new BigDecimal("3150000"), data.getTotalAmount());
        assertEquals(3, data.getNumberOfNights());
    }

    @Test
    void omitsImageAndLocalhostCtaWhenNoPublicImageOrOriginExists() {
        AppProperties localProperties = new AppProperties();
        localProperties.setOrigin("http://localhost:5173");
        dataFactory = new BookingConfirmationEmailDataFactory(bookingRepository, localProperties);
        Booking booking = booking("STANDARD", Set.of(
                RoomTypeImage.builder().url("/images/standard.jpg").build()
        ));
        when(bookingRepository.findConfirmationEmailDetailById("booking-id"))
                .thenReturn(Optional.of(booking));

        BookingConfirmationEmailData data = dataFactory.create("booking-id");

        assertNull(data.getRoomImageUrl());
        assertNull(data.getBookingDetailUrl());
    }

    private Booking booking(String roomTypeName, Set<RoomTypeImage> images) {
        RoomType roomType = RoomType.builder()
                .name(roomTypeName)
                .roomTypeImages(images)
                .build();
        Room room = Room.builder().id(5L).name("501").roomType(roomType).build();
        InvoiceItem roomItem = InvoiceItem.builder()
                .type(InvoiceItemType.ROOM)
                .quantity(3)
                .unitPrice(new BigDecimal("1000000"))
                .build();
        InvoiceItem serviceItem = InvoiceItem.builder()
                .type(InvoiceItemType.SERVICE)
                .extraService(ExtraService.builder().name("Breakfast").build())
                .quantity(2)
                .unitPrice(new BigDecimal("150000"))
                .build();
        Payment payment = Payment.builder()
                .status(PaymentStatus.SUCCESS)
                .method(PaymentMethod.BANK_TRANSFER)
                .amount(new BigDecimal("150000"))
                .paidAt(LocalDateTime.of(2026, 8, 10, 10, 30))
                .build();
        Invoice invoice = Invoice.builder()
                .subtotal(new BigDecimal("3300000"))
                .discountAmount(new BigDecimal("150000"))
                .invoiceItems(Set.of(roomItem, serviceItem))
                .payments(Set.of(payment))
                .invoicePromotions(Set.of(InvoicePromotion.builder()
                        .promotionName("Summer")
                        .build()))
                .build();
        return Booking.builder()
                .id("booking-id")
                .bookingCode("BOOK_20260810")
                .room(room)
                .customer(User.builder().id("customer-id").email("customer@example.com").build())
                .invoice(invoice)
                .checkInDate(LocalDate.of(2026, 8, 15))
                .checkOutDate(LocalDate.of(2026, 8, 18))
                .guestName("Guest")
                .guestPhone("0901234567")
                .guestEmail("guest@example.com")
                .emailLocale(BookingEmailLocale.EN)
                .status(BookingStatus.CONFIRMED)
                .build();
    }
}
