package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceItemCreationData;
import com.hotelmanagement.backend.dto.internal.InvoiceUpdateData;
import com.hotelmanagement.backend.dto.request.InvoiceAddItemRequest;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.*;
import com.hotelmanagement.backend.exception.AppException;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceService {
    InvoiceRepository invoiceRepository;
    InvoiceMapper invoiceMapper;

    InvoiceItemService invoiceItemService;
    ExtraServiceService extraServiceService;
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

    public Invoice getById(String id) {
        return invoiceRepository.findDetailById(id).orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
    }

    public Invoice getMyInvoiceById(String id, String userId) {
        Invoice invoice = getById(id);

        if (invoice.getBooking() == null
                || invoice.getBooking().getCustomer() == null
                || !invoice.getBooking().getCustomer().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return invoice;
    }

    public void reCalculate(Invoice invoice){

        BigDecimal subTotal = BigDecimal.ZERO;

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            BigDecimal itemTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }

        BigDecimal discountAmount = invoice.getDiscountAmount() != null
                                ? invoice.getDiscountAmount()
                                : BigDecimal.ZERO;

        BigDecimal paidAmount = BigDecimal.ZERO;

        for (Payment payment : invoice.getPayments()) {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                paidAmount = paidAmount.add(payment.getAmount());
            }
        }
        BigDecimal remainingAmount = subTotal.subtract(discountAmount).subtract(paidAmount);

        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        invoice.setSubtotal(subTotal);

        invoice.setRemainingAmount(remainingAmount);

        invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice addInvoiceItem(
            String id,
            InvoiceAddItemRequest request
    ) {

        Invoice invoice = getById(id);
        ExtraService extraService =
                extraServiceService.getByid(request.getServiceId());

        InvoiceItem existingItem = null;

        for (InvoiceItem item : invoice.getInvoiceItems()) {
            if (item.getExtraService() != null
                    && item.getExtraService().getId().equals(request.getServiceId())
            ) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            InvoiceItemUpdateRequest updateRequest =
                    InvoiceItemUpdateRequest.builder()
                            .quantity(existingItem.getQuantity() + 1)
                            .build();

            invoiceItemService.update(existingItem.getId(), updateRequest);
        } else {
            InvoiceItemCreationData itemCreationData =
                    InvoiceItemCreationData.builder()
                            .invoice(invoice)
                            .type(
                                    extraService.getType()
                                            == ServiceType.EXTRA_FEE
                                            ? InvoiceItemType.FEE
                                            : InvoiceItemType.SERVICE
                            )
                            .unitPrice(extraService.getBasePrice())
                            .quantity(1)
                            .extraService(extraService)
                            .build();

            InvoiceItem createdItem = invoiceItemService.create(itemCreationData);

            invoice.getInvoiceItems().add(createdItem);

        }
        reCalculate(invoice);

        return invoice;
    }

    public Invoice updateInvoice(String id, InvoiceUpdateData data) {
        Invoice invoice = getById(id);

        invoiceMapper.updateInvoice(invoice, data);

        return invoiceRepository.save(invoice);
    }

    @Transactional
    public Invoice updateInvoiceItem(Long id, InvoiceItemUpdateRequest request) {

        InvoiceItem item = invoiceItemService.update(id, request);

        reCalculate(item.getInvoice());

        return getById(item.getInvoice().getId());
    }

    @Transactional
    public Invoice removeInvoiceItem(Long itemId) {

        InvoiceItem item = invoiceItemService.getById(itemId);

        String invoiceId = item.getInvoice().getId();

        invoiceItemService.deleteById(itemId);

        reCalculate(item.getInvoice());

        return getById(invoiceId);
    }
}
