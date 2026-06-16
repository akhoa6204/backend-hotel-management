package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.PromotionCreationRequest;
import com.hotelmanagement.backend.dto.request.PromotionUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
import com.hotelmanagement.backend.mapper.PromotionMapper;
import com.hotelmanagement.backend.service.PromotionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff/promotions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffPromotionController {
    PromotionService promotionService;
    PromotionMapper promotionMapper;
    @PostMapping("")
    @PreAuthorize("hasAuthority('PROMOTION_CREATE')")
    public ApiResponse<PromotionResponse> create(@RequestBody @Valid PromotionCreationRequest request) {
        PromotionResponse response = promotionMapper.toPromotionResponse(promotionService.create(request));

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION_READ')")
    public ApiResponse<PromotionResponse> getById(@PathVariable Long id) {
        PromotionResponse response = promotionMapper.toPromotionResponse(promotionService.getById(id));

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PROMOTION_READ')")
    public ApiResponse<List<PromotionResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q
    ) {

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                limit
        );

        Page<PromotionResponse> response = promotionService.getList(
                pageRequest,
                q
        ).map(promotionMapper::toPromotionResponse);

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<PromotionResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION_DELETE')")
    public ApiResponse<String> delete(@PathVariable Long id) {
        promotionService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Promotion Successfully")
                .build();
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PROMOTION_UPDATE')")
    public ApiResponse<PromotionResponse> update(@RequestBody PromotionUpdateRequest request, @PathVariable Long id) {
        PromotionResponse response = promotionMapper.toPromotionResponse(promotionService.update(id, request));

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }
}
