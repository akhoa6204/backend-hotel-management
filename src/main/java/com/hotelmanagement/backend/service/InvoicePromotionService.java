package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.internal.InvoicePromotionCreationData;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoicePromotion;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoicePromotionRepository;
import com.hotelmanagement.backend.repository.InvoiceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoicePromotionService {
    InvoicePromotionRepository invoicePromotionRepository;
    public InvoicePromotion create(InvoicePromotionCreationData request) {
        InvoicePromotion invoicePromotion = InvoicePromotion.builder()
                .discountAmount(request.getDiscountAmount())
                .invoice(request.getInvoice())
                .promotionCode(request.getPromotionCode())
                .promotionName(request.getPromotionName())
                .discountValue(request.getDiscountValue())
                .discountType(request.getDiscountType())
                .promotionId(request.getPromotionId())
                .build();
        return invoicePromotionRepository.save(invoicePromotion);
    }
}
