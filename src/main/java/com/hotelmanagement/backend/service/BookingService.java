package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.*;
import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.BookingCreationResponse;
import com.hotelmanagement.backend.dto.response.BookingResponse;
import com.hotelmanagement.backend.dto.response.PricingResultResponse;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
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
import java.util.Optional;
import java.util.Set;

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
    HousekeepingTaskService housekeepingTaskService;

    @Transactional(rollbackFor = Exception.class)
    public BookingCreationResponse create(BookingCreationRequest request) {

        validateBooking(request);

        User staff = request.getStaffId() != null
                ? userService.getById(request.getStaffId())
                : null;

        User customer = request.getCustomerId() != null
                ? userService.getById(request.getCustomerId())
                : null;

        Room room = roomService.findRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate());

        QuoteRequest quoteRequest = QuoteRequest.builder()
                .roomId(request.getRoomId())
                .endDate(request.getCheckOutDate())
                .startDate(request.getCheckInDate())
                .promotionCode(request.getPromotionCode())
                .build();
        PricingResultResponse pricing = pricingService.calculateBookingPrice(quoteRequest);

        BookingCreationData bookingCreationData = BookingCreationData.builder()
                .room(room)
                .customer(customer)
                .staff(staff)
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .estimatedArrivalTime(request.getEstimatedArrivalTime())
                .bookingForSomeoneElse(request.getBookingForSomeoneElse())
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

        savedBooking.setInvoice(savedInvoice);

        InvoiceItemCreationData invoiceItemCreationData = InvoiceItemCreationData.builder()
                .invoice(savedInvoice)
                .type(InvoiceItemType.ROOM)
                .quantity(pricing.getNights())
                .unitPrice(room.getRoomType().getBasePrice())
                .build();

        InvoiceItem savedItem = invoiceItemService.create(invoiceItemCreationData);

        savedInvoice.getInvoiceItems().add(savedItem);

        invoiceService.reCalculate(savedInvoice);

        PromotionResponse manualPromotion = pricing.getPromotion();
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

        PromotionResponse autoPromotion = pricing.getAutoPromotion();
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

        invoiceService.reCalculate(savedInvoice);
        BookingCreationResponse response = BookingCreationResponse.builder()
                .invoiceId(savedInvoice.getId())
                .bookingId(savedBooking.getId())
                .remainingAmount(savedInvoice.getRemainingAmount())
                .build();
        return response;
    }

    public Page<Booking> getList(
            PageRequest request,
            String q
            ){
        return bookingRepository.getItemsWithParams(q, request);
    }

    public Booking getEntityById(String id) {
        return bookingRepository.findByIdAndStatusNot(id, BookingStatus.NO_SHOW)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

    public BookingResponse getByid(String id) {
        Booking booking = getEntityById(id);

        BookingResponse response = bookingMapper.toBookingResponse(booking);

        Optional<HousekeepingTask> inspectionTaskOpt =
                housekeepingTaskService.findInspectionTaskByBookingId(id);

        if (inspectionTaskOpt.isPresent()) {
            HousekeepingTask inspectionTask = inspectionTaskOpt.get();

            response.setInspectionTaskId(inspectionTask.getId());

            response.setInspected(
                    inspectionTask.getStatus() == HousekeepingTaskStatus.COMPLETED
            );
        } else {
            response.setInspectionTaskId(null);
            response.setInspected(false);
        }

        return response;
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

    public Booking updateBooking(String id, BookingUpdateRequest request) {
        Booking booking = getEntityById(id);
        if (booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        LocalDate now  = LocalDate.now();
        boolean isFutureCheckInDate = booking.getCheckInDate().isAfter(now);
        boolean isCheckedIn = booking.getStatus() == BookingStatus.CHECKED_IN;

        LocalDate checkinDate = isFutureCheckInDate
                ? booking.getCheckInDate()
                : now;

        Room room = roomService.findRoomAvailable(
                request.getRoomId(),
                checkinDate,
                booking.getCheckOutDate()
        );

        if (isCheckedIn) {
            RoomUpdateRequest oldRoomUpdateRequest = RoomUpdateRequest.builder()
                    .status(RoomStatus.VACANT_DIRTY)
                    .build();

            roomService.updateRoom(booking.getRoom().getId(), oldRoomUpdateRequest);

            HousekeepingTaskCreationRequest task = HousekeepingTaskCreationRequest.builder()
                    .roomId(booking.getRoom().getId())
                    .type(HousekeepingTaskType.CLEANING)
                    .build();

            housekeepingTaskService.createTask(task);

            RoomUpdateRequest newRoomUpdateRequest = RoomUpdateRequest.builder()
                    .status(RoomStatus.OCCUPIED_CLEAN)
                    .build();

            roomService.updateRoom(room.getId(), newRoomUpdateRequest);
        }


        booking.setRoom(room);

        bookingRepository.save(booking);

        return booking;
    }
    @Transactional
    public Booking confirmBooking(String id){
        Booking  booking = getEntityById(id);
        Invoice invoice = booking.getInvoice();
        Set<Payment> payments = invoice.getPayments();
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }
        boolean hasSuccessPayment = payments.stream()
                .anyMatch(payment ->
                        payment.getStatus() == PaymentStatus.SUCCESS);

        if (!hasSuccessPayment) {
            throw new AppException(ErrorCode.PAYMENT_REQUIRED_TO_CONFIRM_BOOKING);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        invoice.setStatus(InvoiceStatus.ACTIVE);
        return bookingRepository.save(booking);
    }

    public Booking cancelBooking(String id) {
        Booking booking = getEntityById(id);
        Invoice invoice = booking.getInvoice();
        booking.setStatus(BookingStatus.CANCELLED);
        invoice.setStatus(InvoiceStatus.CANCELLED);
        return bookingRepository.save(booking);
    }
    @Transactional
    public Booking checkoutBooking(String id){
        Booking booking = getEntityById(id);

        if (booking.getStatus() != BookingStatus.CHECKED_IN) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }

        boolean hasCompletedInspectionTask = housekeepingTaskService.hasCompletedInspectionTask(id);

        if(!hasCompletedInspectionTask) {
            throw new AppException(ErrorCode.INSPECTION_TASK_REQUIRED_FOR_CHECKOUT);
        }

        Invoice invoice = booking.getInvoice();

        BigDecimal totalPaidAmount = invoice.getPayments()
                .stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = invoice.getSubtotal()
                .subtract(invoice.getDiscountAmount());

        if (totalPaidAmount.compareTo(finalTotal) < 0) {
            throw new AppException(ErrorCode.INVOICE_NOT_FULLY_PAID);
        }

        booking.setStatus(BookingStatus.CHECKED_OUT);
        invoice.setStatus(InvoiceStatus.DONE);
        invoice.setRemainingAmount(BigDecimal.ZERO);

        RoomUpdateRequest roomUpdateRequest =
                RoomUpdateRequest.builder()
                        .status(RoomStatus.VACANT_DIRTY)
                        .build();

        roomService.updateRoom(booking.getRoom().getId(), roomUpdateRequest);

        HousekeepingTaskCreationRequest task = HousekeepingTaskCreationRequest.builder()
                .bookingId(id)
                .roomId(booking.getRoom().getId())
                .type(HousekeepingTaskType.CLEANING)
                .build();

        housekeepingTaskService.createTask(task);

        return bookingRepository.save(booking);
    }
    @Transactional
    public Booking checkinBooking(String id){
        Booking booking = getEntityById(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new AppException(ErrorCode.INVALID_BOOKING_STATUS);
        }
        Invoice invoice = booking.getInvoice();
        Set<Payment> payments = invoice.getPayments();
        BigDecimal totalSuccessRoomPayment = payments.stream()
                .filter(payment ->
                        payment.getStatus() == PaymentStatus.SUCCESS
                                && (
                                    payment.getType() == PaymentType.ROOM_PAYMENT
                                    || payment.getType() == PaymentType.DEPOSIT
                                )
                )
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRoomAmount = invoice.getInvoiceItems()
                .stream()
                .filter(item -> item.getType() == InvoiceItemType.ROOM)
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .subtract(invoice.getDiscountAmount());

        if (totalRoomAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalRoomAmount = BigDecimal.ZERO;
        }

        boolean hasSuccessRoomPayment =
                totalSuccessRoomPayment.compareTo(totalRoomAmount) >= 0;
        if (!hasSuccessRoomPayment) {
            throw new AppException(ErrorCode.ROOM_PAYMENT_REQUIRED_FOR_CHECKIN);
        }
        booking.setStatus(BookingStatus.CHECKED_IN);
        invoice.setStatus(InvoiceStatus.ACTIVE);

        RoomUpdateRequest roomUpdateRequest =
                RoomUpdateRequest.builder()
                        .status(RoomStatus.OCCUPIED_CLEAN)
                        .build();

        roomService.updateRoom(
                booking.getRoom().getId(),
                roomUpdateRequest
        );

        return bookingRepository.save(booking);
    }

    public PricingResultResponse quote(QuoteRequest request){
        return pricingService.calculateBookingPrice(request);
    }

}
