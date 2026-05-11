package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.Utils.DateUtils;
import com.hotelmanagement.backend.dto.internal.*;
import com.hotelmanagement.backend.dto.request.BookingCreationRequest;
import com.hotelmanagement.backend.dto.response.BookingResponse;
import com.hotelmanagement.backend.entity.*;
import com.hotelmanagement.backend.enums.*;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.BookingMapper;
import com.hotelmanagement.backend.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
    BookingRepository bookingRepository;
    BookingMapper bookingMapper;

    UserService userService;
    InvoiceService invoiceService;
    RoomService roomService;
    InvoiceItemService invoiceItemService;
    PricingService pricingService;
    InvoicePromotionService invoicePromotionService;

    @Transactional(rollbackFor = Exception.class)
    public Booking create(BookingCreationRequest request) {

        validateBooking(request);

        User staff = request.getStaffId() != null
                ? userService.getById(request.getStaffId())
                : null;

        User customer = request.getCustomerId() != null
                ? userService.getById(request.getCustomerId())
                : null;

        Room room = roomService.getByid(request.getRoomId());

        PricingResult pricing = pricingService.calculateBookingPrice(request, room);

        BookingCreationData bookingCreationData = BookingCreationData.builder()
                .room(room)
                .customer(customer)
                .staff(staff)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .estimatedArrivalTime(request.getEstimatedArrivalTime())
                .bookingForSomeoneElse(request.isBookingForSomeoneElse())
                .guestName(request.getGuestName())
                .guestPhone(request.getGuestPhone())
                .guestEmail(request.getGuestEmail())
                .build();

        Booking savedBooking = createEntityBooking(bookingCreationData);

        InvoiceCreationData invoiceCreationData = InvoiceCreationData.builder()
                .booking(savedBooking)
                .subtotal(pricing.getSubtotal())
                .discountAmount(pricing.getTotalDiscount())
                .remainingAmount(pricing.getFinalTotal())
                .build();

        Invoice savedInvoice = invoiceService.create(invoiceCreationData);

        InvoiceItemCreationData invoiceItemCreationData = InvoiceItemCreationData.builder()
                .invoice(savedInvoice)
                .type(InvoiceItemType.ROOM)
                .quantity(pricing.getNights())
                .unitPrice(room.getRoomType().getBasePrice())
                .build();

        invoiceItemService.create(invoiceItemCreationData);

        Promotion manualPromotion = pricing.getPromotion();
        if( manualPromotion != null ) {
            InvoicePromotionCreationData invoiceManualPromotionCreationData = InvoicePromotionCreationData.builder()
                    .invoice(savedInvoice)
                    .promotionId(manualPromotion.getId())
                    .promotionCode(manualPromotion.getCode())
                    .promotionName(manualPromotion.getName())
                    .discountType(manualPromotion.getDiscountType())
                    .discountValue(manualPromotion.getDiscountValue())
                    .discountAmount(manualPromotion.getDiscountValue())
                    .build();
            invoicePromotionService.create(invoiceManualPromotionCreationData);
        }

        Promotion autoPromotion = pricing.getAutoPromotion();
        if( autoPromotion != null ) {
            InvoicePromotionCreationData invoiceAutoPromotionCreationData = InvoicePromotionCreationData.builder()
                    .invoice(savedInvoice)
                    .promotionId(autoPromotion.getId())
                    .promotionCode(autoPromotion.getCode())
                    .promotionName(autoPromotion.getName())
                    .discountType(autoPromotion.getDiscountType())
                    .discountValue(autoPromotion.getDiscountValue())
                    .discountAmount(autoPromotion.getDiscountValue())
                    .build();
            invoicePromotionService.create(invoiceAutoPromotionCreationData);
        }
        return savedBooking;
    }

    public Page<Booking> getList(
            PageRequest request,
            String q
            ){
        return bookingRepository.getItemsWithParams(q, request);
    }

    public Booking getByid (String id){
        Booking booking = bookingRepository.findByIdAndStatusNot(id, BookingStatus.NO_SHOW)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
        return booking;
    }

    private Booking createEntityBooking(BookingCreationData request){
        String bookingCode = "BOOK_" + System.currentTimeMillis();
        Booking booking = bookingMapper.toBooking(request);
        booking.setBookingCode(bookingCode);
        booking.setRefundable(false);
        booking.setStatus(BookingStatus.PENDING);

        return bookingRepository.save(booking);
    }

    private void validateBooking(BookingCreationRequest request) {
        if (request.getCheckInDate().isAfter(request.getCheckOutDate())) {
            throw new AppException(
                    ErrorCode.INVALID_BOOKING_DATE
            );
        }

        if (bookingRepository.existsBookingOverlap(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate()
        )) {
            throw new AppException(
                    ErrorCode.BOOKING_ALREADY_EXISTS
            );
        }
    }
}
