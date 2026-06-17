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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    PromotionService promotionService;
    CancelReasonRepository cancelReasonRepository;
    @Transactional(rollbackFor = Exception.class)
    public BookingResponse create(BookingCreationRequest request) {

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
                    .discountAmount(pricing.getPromotionDiscount())
                    .build();
            invoicePromotionService.create(invoiceManualPromotionCreationData);

            promotionService.increaseQuota(manualPromotion.getId());
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
                    .discountAmount(pricing.getAutoDiscount())
                    .build();
            invoicePromotionService.create(invoiceAutoPromotionCreationData);
            promotionService.increaseQuota(autoPromotion.getId());
        }

        invoiceService.reCalculate(savedInvoice);

        BookingResponse response = bookingMapper.toBookingResponse(savedBooking);
        response.setFinalAmount(
                savedInvoice.getSubtotal()
                        .subtract(savedInvoice.getDiscountAmount())
        );
        response.setRemainingAmount(savedInvoice.getRemainingAmount());
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

    public BookingResponse getById(String id) {
        Booking booking = getEntityById(id);
        return bookingMapper.toBookingResponse(booking);
    }

    public BookingResponse getStaffBookingById(String id) {
        Booking booking = getEntityById(id);
        BookingResponse response = bookingMapper.toBookingResponse(booking);

        Optional<HousekeepingTask> inspectionTaskOpt =
                housekeepingTaskService.findInspectionTaskByBookingId(booking.getId());

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

    public BookingResponse getMyBookingById(String userId, String id) {
        Booking booking = getEntityById(id);
        validateCurrentUserOwnsBooking(userId, booking);
        return toMyBookingResponse(booking);
    }

    private BookingResponse toMyBookingResponse(Booking booking) {
        BookingResponse response = bookingMapper.toBookingResponse(booking);
        if (booking.getInvoice() != null) {
            Invoice invoice = booking.getInvoice();

            BigDecimal roomAmount = invoice.getInvoiceItems()
                    .stream()
                    .filter(item -> item.getType() == InvoiceItemType.ROOM)
                    .map(item -> item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal roomDiscountAmount = invoice.getDiscountAmount() == null
                    ? BigDecimal.ZERO
                    : invoice.getDiscountAmount();

            BigDecimal roomFinalAmount = roomAmount.subtract(roomDiscountAmount);
            if (roomFinalAmount.compareTo(BigDecimal.ZERO) < 0) {
                roomFinalAmount = BigDecimal.ZERO;
            }

            BigDecimal depositPaidAmount = invoice.getPayments()
                    .stream()
                    .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                    .filter(payment -> payment.getType() == PaymentType.DEPOSIT)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal roomPaymentPaidAmount = invoice.getPayments()
                    .stream()
                    .filter(payment -> payment.getStatus() == PaymentStatus.SUCCESS)
                    .filter(payment -> payment.getType() == PaymentType.ROOM_PAYMENT
                            || payment.getType() == PaymentType.DEPOSIT)
                    .map(Payment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            response.setRoomAmount(roomAmount);
            response.setRoomDiscountAmount(roomDiscountAmount);
            response.setRoomFinalAmount(roomFinalAmount);
            response.setDepositPaidAmount(depositPaidAmount);
            response.setRoomPaymentPaidAmount(roomPaymentPaidAmount);
            response.setFinalAmount(
                    invoice.getSubtotal()
                            .subtract(invoice.getDiscountAmount())
            );
            response.setRemainingAmount(invoice.getRemainingAmount());
        }

        Optional<HousekeepingTask> inspectionTaskOpt =
                housekeepingTaskService.findInspectionTaskByBookingId(booking.getId());

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

    private void validateCurrentUserOwnsBooking(String userId, Booking booking) {
        if (booking.getCustomer() == null
                || booking.getCustomer().getId() == null
                || !booking.getCustomer().getId().equals(userId)) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND);
        }
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

    @Transactional
    public Booking cancelBooking(String userId, String id, BookingCancelRequest request) {
        Booking booking = getEntityById(id);
        User user = userService.getById(userId);
        return cancelBookingEntity(user, booking, request);
    }

    @Transactional
    public Booking cancelMyBooking(String userId, String id, BookingCancelRequest request) {
        Booking booking = getEntityById(id);
        validateCurrentUserOwnsBooking(userId, booking);
        User user = userService.getById(userId);
        return cancelBookingEntity(user, booking, request);
    }

    private Booking cancelBookingEntity(User user, Booking booking, BookingCancelRequest request) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new AppException(ErrorCode.BOOKING_ALREADY_CANCELLED);
        }
        Invoice invoice = booking.getInvoice();
        booking.setStatus(BookingStatus.CANCELLED);
        if (invoice != null) {
            invoice.setStatus(InvoiceStatus.CANCELLED);
        }

        if (cancelReasonRepository.existsByBookingId(booking.getId())) {
            throw new AppException(ErrorCode.CANCEL_REASON_ALREADY_EXISTS);
        }
        boolean isAdmin = !user.getRole().getName().equals(UserRole.USER.name());
        CancelReason cancelReason = CancelReason.builder()
                .booking(booking)
                .reason(request.getReason())
                .cancelledBy(user)
                .staffCancel(isAdmin)
                .build();
        cancelReasonRepository.save(cancelReason);
        booking.setCancelReason(cancelReason);
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
        LocalDateTime now  = LocalDateTime.now();
        booking.setStatus(BookingStatus.CHECKED_OUT);
        invoice.setStatus(InvoiceStatus.DONE);
        invoice.setRemainingAmount(BigDecimal.ZERO);
        invoice.setPaidAt(now);
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

    public Page<BookingResponse> getMyList(
            String userId,
            PageRequest request,
            String q
    ) {
        return bookingRepository.getMyItemsWithParams(userId, q, request)
                .map(this::toMyBookingResponse);
    }
}
