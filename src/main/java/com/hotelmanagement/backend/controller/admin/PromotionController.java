package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.PromotionCreationRequest;
import com.hotelmanagement.backend.dto.request.PromotionUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.service.PromotionService;
import com.hotelmanagement.backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/promotions")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PromotionController {
    PromotionService promotionService;

    @PostMapping("")
    public ApiResponse<PromotionResponse> createRoom(@RequestBody @Valid PromotionCreationRequest request) {
        PromotionResponse response = promotionService.create(request);

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PromotionResponse> getById(@PathVariable Long id) {
        PromotionResponse response = promotionService.getById(id);

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }

    @GetMapping
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
        );

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
    public ApiResponse<String> delete(@PathVariable Long id) {
        promotionService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Promotion Successfully")
                .build();
    }


    @PutMapping("/{id}")
    public ApiResponse<PromotionResponse> updateRoomType(@RequestBody PromotionUpdateRequest request, @PathVariable Long id) {
        PromotionResponse response = promotionService.update(id, request);

        return ApiResponse.<PromotionResponse>builder().data(response).build();
    }
}
