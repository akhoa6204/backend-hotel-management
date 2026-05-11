package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoiceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceService {
    InvoiceRepository invoiceRepository;
    BookingRepository bookingRepository;
    InvoiceMapper invoiceMapper;
    public Invoice create(InvoiceCreationData request) {
        String invoiceCode = "INV_" + System.currentTimeMillis();

        Invoice invoice = Invoice.builder()
                .invoiceCode(invoiceCode)
                .booking(request.getBooking())
                .status(InvoiceStatus.PENDING)
                .subtotal(request.getSubtotal())
                .discountAmount(request.getDiscountAmount())
                .remainingAmount(request.getRemainingAmount())
                .issuedAt(LocalDateTime.now())
                .build();
        return invoiceRepository.save(invoice);
    }

    public Page<Invoice> getList(PageRequest pageRequest, String q) {
        return invoiceRepository.findByInvoiceCodeContaining(q, pageRequest);
    }
}
