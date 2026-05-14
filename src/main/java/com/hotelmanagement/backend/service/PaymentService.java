package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceItemCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceUpdateData;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.dto.request.PaymentCreationRequest;
import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.dto.response.PaymentResponse;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.mapper.PaymentMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoiceItemRepository;
import com.hotelmanagement.backend.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    InvoiceService invoiceService;
    PaymentRepository paymentRepository;
    PaymentMapper paymentMapper;
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
                .build();
        return paymentRepository.save(payment);
    }

    public Payment update(Long id, PaymentUpdateRequest request) {
        Payment payment = getById(id);
        paymentMapper.updatePayment(payment, request);
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            LocalDateTime  now = LocalDateTime.now();
            payment.setPaidAt(now);
            InvoiceUpdateData invoiceUpdateData = InvoiceUpdateData.builder()
                    .invoiceStatus(InvoiceStatus.ACTIVE)
                    .build();
            invoiceService.updateInvoice(payment.getInvoice().getId(), invoiceUpdateData);
        }
        invoiceService.reCalculate(payment.getInvoice());

        return paymentRepository.save(payment);
    }

}
