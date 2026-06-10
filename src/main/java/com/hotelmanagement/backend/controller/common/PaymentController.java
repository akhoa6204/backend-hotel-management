package com.hotelmanagement.backend.controller.common;

import com.hotelmanagement.backend.dto.request.InvoiceAddItemRequest;
import com.hotelmanagement.backend.dto.request.PaymentCreationRequest;
import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.dto.response.*;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.mapper.PaymentMapper;
import com.hotelmanagement.backend.service.InvoiceService;
import com.hotelmanagement.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PaymentController {
    PaymentService paymentService;
    PaymentMapper paymentMapper;

    @PatchMapping("/{id}")
    public ApiResponse<PaymentResponse> update(@PathVariable Long id, @RequestBody @Valid PaymentUpdateRequest request) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.update(id, request));

        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("")
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentCreationRequest request) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.create(request));
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @PostMapping("/{paymentId}/checkout-link")
    public ApiResponse<CheckoutLinkResponse> checkoutLink(@PathVariable Long paymentId) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_HOUSEKEEPING")
                        || a.getAuthority().equals("ROLE_RECEPTIONIST"));

        CheckoutLinkResponse response =
                paymentService.createCheckoutLink(paymentId, isAdmin);
        return ApiResponse.<CheckoutLinkResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentResponse> getByid(@PathVariable Long id) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.getById(id));
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }
}
