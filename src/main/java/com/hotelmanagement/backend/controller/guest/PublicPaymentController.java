package com.hotelmanagement.backend.controller.guest;

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

@RestController
@RequestMapping("/public/payments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicPaymentController {
    PaymentService paymentService;
    PaymentMapper paymentMapper;

    @PostMapping("")
    public ApiResponse<PaymentResponse> create(@Valid @RequestBody PaymentCreationRequest request) {
        PaymentResponse response = paymentMapper.toPaymentResponse(paymentService.create(request));
        return ApiResponse.<PaymentResponse>builder()
                .data(response)
                .build();
    }

    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentResponse> getById(@PathVariable Long paymentId) {
        PaymentResponse response =
                paymentMapper.toPaymentResponse(paymentService.getById(paymentId));

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



}
