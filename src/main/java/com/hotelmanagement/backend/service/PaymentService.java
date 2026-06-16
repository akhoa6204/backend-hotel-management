package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.config.AppProperties;
import com.hotelmanagement.backend.dto.internal.InvoiceItemCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceUpdateData;
import com.hotelmanagement.backend.dto.request.BookingUpdateRequest;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.dto.request.PaymentCreationRequest;
import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.dto.response.CheckoutLinkResponse;
import com.hotelmanagement.backend.dto.response.PaymentResponse;
import com.hotelmanagement.backend.dto.response.SePayCheckoutResponse;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.*;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.mapper.PaymentMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoiceItemRepository;
import com.hotelmanagement.backend.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    BookingService bookingService;
    InvoiceService invoiceService;
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;

    SePayService sePayService;

    Pattern SEPAY_PAYMENT_CODE_PATTERN = Pattern.compile("DH(\\d+)");

    public Payment getById(Long id){
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new AppException(ErrorCode.PAYMENT_NOT_FOUND));
    }

    public Payment create(PaymentCreationRequest request) {
        String paymentCode = "PAY_" + System.currentTimeMillis();
        Invoice invoice = invoiceService.getById(request.getInvoiceId());
        if(invoice.getStatus().equals(InvoiceStatus.DONE)){
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }else if(invoice.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        Payment payment = Payment.builder()
                .paymentCode(paymentCode)
                .invoice(invoice)
                .amount(request.getAmount())
                .status(PaymentStatus.PENDING)
                .method(request.getPaymentMethod())
                .type(request.getPaymentType())
                .paidAt(null)
                .expiredAt(LocalDateTime.now().plusMinutes(15))
                .build();
        return paymentRepository.save(payment);
    }

    public Payment update(Long id, PaymentUpdateRequest request) {
        Payment payment = getById(id);
        paymentMapper.updatePayment(payment, request);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            payment = handleSuccessfulPayment(
                    payment,
                    payment.getTransactionCode()
            );
        }

        invoiceService.reCalculate(payment.getInvoice());

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return payment;
        }

        return paymentRepository.save(payment);
    }

    public CheckoutLinkResponse createCheckoutLink(Long paymentId, boolean isAdmin) {
        Payment payment = getById(paymentId);

        if (payment.getMethod() != PaymentMethod.BANK_TRANSFER) {
            throw new AppException(ErrorCode.PAYMENT_NOT_TRANSFER);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new AppException(ErrorCode.PAYMENT_INVALID_STATUS);
        }

        Long testAmount = 2000L;
        String transferContent = String.format("DH%s",payment.getId()) ;

        SePayCheckoutResponse result = sePayService.buildBankTransferQr(
                payment.getId(),
                testAmount,
                transferContent
        );

        return CheckoutLinkResponse.builder()
                .qrUrl(result.getQrUrl())
                .paymentId(payment.getId())
                .build();
    }


    public PaymentResponse handleSePayWebhook(Map<String, Object> payload) {
        String code = getStringValue(payload, "code");

        if (code == null || code.isBlank()) {
            code = getStringValue(payload, "content");
        }

        if (code == null || code.isBlank()) {
            code = getStringValue(payload, "description");
        }

        if (code == null || code.isBlank()) {
            throw new RuntimeException("Missing SePay transfer content");
        }

        Matcher matcher = SEPAY_PAYMENT_CODE_PATTERN.matcher(code);

        if (!matcher.find()) {
            throw new RuntimeException("Cannot detect payment id from SePay transfer content: " + code);
        }

        Long paymentId = Long.valueOf(matcher.group(1));
        Payment payment = getById(paymentId);

        String transactionCode = getStringValue(payload, "referenceCode");
        if (transactionCode == null || transactionCode.isBlank()) {
            transactionCode = getStringValue(payload, "transactionCode");
        }

        Payment savedPayment = handleSuccessfulPayment(payment, transactionCode);

        return paymentMapper.toPaymentResponse(savedPayment);
    }

    public PaymentResponse handleRefundSePayWebhook(Map<String, Object> payload) {
        String code = getStringValue(payload, "code");

        if (code == null || code.isBlank()) {
            code = getStringValue(payload, "content");
        }

        if (code == null || code.isBlank()) {
            code = getStringValue(payload, "description");
        }

        if (code == null || code.isBlank()) {
            throw new RuntimeException("Missing SePay refund transfer content");
        }

        Matcher matcher = SEPAY_PAYMENT_CODE_PATTERN.matcher(code);

        if (!matcher.find()) {
            throw new RuntimeException("Cannot detect refund payment id from SePay transfer content: " + code);
        }

        Long paymentId = Long.valueOf(matcher.group(1));
        Payment payment = getById(paymentId);

        if (payment.getType() != PaymentType.REFUND) {
            throw new RuntimeException("Payment is not a refund payment");
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            return paymentMapper.toPaymentResponse(payment);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Refund payment is not waiting for refund confirmation");
        }

        String transactionCode = getStringValue(payload, "referenceCode");
        if (transactionCode == null || transactionCode.isBlank()) {
            transactionCode = getStringValue(payload, "transactionCode");
        }

        BigDecimal transferAmount = getBigDecimalValue(payload, "transferAmount");
        if (transferAmount != null) {
            payment.setAmount(transferAmount);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionCode(transactionCode);

        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toPaymentResponse(savedPayment);
    }

    private Payment handleSuccessfulPayment(
            Payment payment,
            String transactionCode
    ) {
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        if (transactionCode != null && !transactionCode.isBlank()) {
            payment.setTransactionCode(transactionCode);
        }

        Payment savedPayment = paymentRepository.save(payment);

        Invoice invoice = savedPayment.getInvoice();
        Booking booking = invoice.getBooking();

        if (invoice.getStatus() != InvoiceStatus.ACTIVE
                && invoice.getStatus() != InvoiceStatus.DONE) {

            InvoiceUpdateData invoiceUpdateData = InvoiceUpdateData.builder()
                    .invoiceStatus(InvoiceStatus.ACTIVE)
                    .build();

            invoiceService.updateInvoice(invoice.getId(), invoiceUpdateData);
        }

        if (booking.getStatus() == BookingStatus.PENDING) {
            bookingService.confirmBooking(booking.getId());
        }

        invoice = invoiceService.getById(invoice.getId());
        invoiceService.reCalculate(invoice);

        BigDecimal remainingAmount = invoice.getRemainingAmount();

        if (remainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            failOtherPendingPayments(invoice.getId(), savedPayment.getId());
        }

        return savedPayment;
    }

    private void failOtherPendingPayments(String invoiceId, Long successPaymentId) {
        List<Payment> pendingPayments = paymentRepository.findOtherPendingPaymentsByInvoiceId(
                invoiceId,
                successPaymentId
        );

        for (Payment pendingPayment : pendingPayments) {
            pendingPayment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.saveAll(pendingPayments);
    }

    private String getStringValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value == null) {
            return null;
        }
        return new BigDecimal(String.valueOf(value));
    }
}
