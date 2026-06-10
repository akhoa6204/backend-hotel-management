package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.request.PaymentCreationRequest;
import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.CheckoutLinkResponse;
import com.hotelmanagement.backend.dto.response.PaymentResponse;
import com.hotelmanagement.backend.mapper.PaymentMapper;
import com.hotelmanagement.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class WebhookController {
    PaymentService paymentService;
    PaymentMapper paymentMapper;

    @PostMapping("/sepay")
    public ApiResponse<PaymentResponse> handleSePayWebhook(@RequestBody Map<String, Object> payload) {
        PaymentResponse response = paymentService.handleSePayWebhook(payload);
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/refund-sepay")
    public ApiResponse<PaymentResponse> handleRefundSePayWebhook(@RequestBody Map<String, Object> payload) {
        PaymentResponse response = paymentService.handleRefundSePayWebhook(payload);
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }
}
