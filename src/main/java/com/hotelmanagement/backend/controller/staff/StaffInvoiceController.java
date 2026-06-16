package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.InvoiceAddItemRequest;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.InvoiceResponse;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/invoices")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffInvoiceController {
    InvoiceService invoiceService;
    InvoiceMapper invoiceMapper;

    @PreAuthorize("hasAuthority('INVOICE_READ')")
    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getById(
           @PathVariable String id
    ){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.getById(id));

        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }

    @PreAuthorize("hasAuthority('INVOICE_ITEM_CREATE')")
    @PostMapping("/{id}/invoice-items")
    public ApiResponse<InvoiceResponse> updateAddInvoiceItem(
            @PathVariable String id,
            @RequestBody @Valid InvoiceAddItemRequest request){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.addInvoiceItem(id, request));

        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }

    @PreAuthorize("hasAuthority('INVOICE_ITEM_UPDATE')")
    @PatchMapping("/invoice-items/{id}")
    public ApiResponse<InvoiceResponse> updateInvoiceItem(
            @PathVariable Long id,
            @RequestBody @Valid InvoiceItemUpdateRequest request
    ){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.updateInvoiceItem(id, request));
        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }

    @PreAuthorize("hasAuthority('INVOICE_ITEM_DELETE')")
    @DeleteMapping("/invoice-items/{id}")
    public ApiResponse<InvoiceResponse> deleteItem(
            @PathVariable Long id
    ){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.removeInvoiceItem(id));
        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }
}
