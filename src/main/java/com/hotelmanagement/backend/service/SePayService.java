package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.config.SePayProperties;
import com.hotelmanagement.backend.dto.response.SePayCheckoutResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class SePayService {

    private final SePayProperties properties;

    public SePayCheckoutResponse buildBankTransferQr(
            Long id,
            Long amount,
            String description
    ) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("amount must be positive");
        }

        String accountNumber = "05626027210";
        String bankCode = "TPB";
        String template = "compact";
        String download = "false";
        String transferDescription = description != null && !description.isBlank()
                ? description
                : String.format("DH%s",id);

        String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%s&des=%s&template=%s&download=%s",
                encode(accountNumber),
                encode(bankCode),
                amount,
                encode(transferDescription),
                encode(template),
                encode(download)
        );

        return SePayCheckoutResponse.builder()
                .qrUrl(qrUrl)
                .build();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}