package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.response.PaymentResponse;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.mapper.PaymentMapper;
import com.hotelmanagement.backend.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentWebhookIdempotencyTest {
    @Mock BookingService bookingService;
    @Mock InvoiceService invoiceService;
    @Mock PaymentRepository paymentRepository;
    @Mock PaymentMapper paymentMapper;
    @Mock SePayService sePayService;

    @InjectMocks PaymentService paymentService;

    @Test
    void repeatedSuccessfulWebhookDoesNotConfirmOrSaveAgain() {
        Payment payment = Payment.builder().id(12L).status(PaymentStatus.SUCCESS).build();
        PaymentResponse response = PaymentResponse.builder().id(12L).status(PaymentStatus.SUCCESS).build();
        when(paymentRepository.findById(12L)).thenReturn(Optional.of(payment));
        when(paymentMapper.toPaymentResponse(payment)).thenReturn(response);

        PaymentResponse result = paymentService.handleSePayWebhook(Map.of("content", "DH12"));

        assertSame(response, result);
        verify(paymentRepository, never()).save(payment);
        verify(bookingService, never()).confirmBooking("booking-id");
    }
}
