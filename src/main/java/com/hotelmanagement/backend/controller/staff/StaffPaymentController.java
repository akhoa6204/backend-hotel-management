package com.hotelmanagement.backend.controller.staff;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/payments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffPaymentController {
    PaymentService paymentService;
    PaymentMapper paymentMapper;

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_UPDATE')")
    public ApiResponse<PaymentResponse> update(@PathVariable Long id, @RequestBody @Valid PaymentUpdateRequest request) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.update(id, request));

        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentCreationRequest request) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.create(request));
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/{paymentId}/checkout-link")
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public ApiResponse<CheckoutLinkResponse> checkoutLink(@PathVariable Long paymentId) {
        CheckoutLinkResponse response = paymentService.createCheckoutLink(paymentId, true);
        return ApiResponse.<CheckoutLinkResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    public ApiResponse<PaymentResponse> getByid(@PathVariable Long id) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.getById(id));
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }
}
