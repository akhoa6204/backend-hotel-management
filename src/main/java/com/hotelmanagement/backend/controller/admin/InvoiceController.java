package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.InvoiceAddItemRequest;
import com.hotelmanagement.backend.dto.request.InvoiceItemUpdateRequest;
import com.hotelmanagement.backend.dto.request.PromotionCreationRequest;
import com.hotelmanagement.backend.dto.request.PromotionUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.InvoiceResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
import com.hotelmanagement.backend.mapper.InvoiceMapper;
import com.hotelmanagement.backend.mapper.PromotionMapper;
import com.hotelmanagement.backend.service.InvoiceService;
import com.hotelmanagement.backend.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class InvoiceController {
    InvoiceService invoiceService;
    InvoiceMapper invoiceMapper;
    @GetMapping("")
    public ApiResponse<List<InvoiceResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q
            ){
        PageRequest pageable = PageRequest.of(page -1 , limit);
        Page<InvoiceResponse> response = invoiceService.getList(pageable, q).map(invoiceMapper::toInvoiceResponse);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();
        return ApiResponse.<List<InvoiceResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<InvoiceResponse> getList(
           @PathVariable String id
    ){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.getById(id));

        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }


    @PostMapping("/{id}/invoice-items")
    public ApiResponse<InvoiceResponse> updateAddInvoiceItem(
            @PathVariable String id,
            @RequestBody @Valid InvoiceAddItemRequest request){
        InvoiceResponse response = invoiceMapper.toInvoiceResponse(invoiceService.addInvoiceItem(id, request));

        return ApiResponse.<InvoiceResponse>builder()
                .data(response)
                .build();
    }

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
