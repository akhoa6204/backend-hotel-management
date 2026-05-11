package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceItemCreationData;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.InvoiceItemRepository;
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
public class InvoiceItemService {
    InvoiceItemRepository invoiceItemRepository;
    BookingRepository bookingRepository;
    InvoiceMapper invoiceMapper;
    public InvoiceItem create(InvoiceItemCreationData request) {
        InvoiceItem invoiceItem = InvoiceItem.builder()
                .invoice(request.getInvoice())
                .type(request.getType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();
        return invoiceItemRepository.save(invoiceItem);
    }
}
