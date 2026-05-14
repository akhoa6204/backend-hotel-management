package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceItemCreationData;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import com.hotelmanagement.backend.exception.AppException;
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

    public InvoiceItem create(InvoiceItemCreationData request) {
        InvoiceItem invoiceItem = InvoiceItem.builder()
                .invoice(request.getInvoice())
                .type(request.getType())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .extraService(request.getExtraService())
                .build();
        return invoiceItemRepository.save(invoiceItem);
    }

    public InvoiceItem update(Long id, InvoiceItemUpdateRequest request) {
        InvoiceItem item = getById(id);

        item.setQuantity(request.getQuantity());

        return invoiceItemRepository.save(item);
    }

    public void deleteById(Long id) {
        InvoiceItem item = invoiceItemRepository
                .findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_ITEM_NOT_FOUND));

        invoiceItemRepository.delete(item);
    }

    public InvoiceItem getById(Long id){
        return invoiceItemRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.INVOICE_ITEM_NOT_FOUND));
    }

}
