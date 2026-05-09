package com.hotelmanagement.backend.controller.admin;

import com.hotelmanagement.backend.dto.request.ExtraServiceCreationRequest;
import com.hotelmanagement.backend.dto.request.ExtraServiceUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.ExtraServiceResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.enums.ServiceType;
import com.hotelmanagement.backend.service.ExtraServiceService;
import com.hotelmanagement.backend.service.RoomService;
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
@RequestMapping("/services")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ExtraServiceController {
    ExtraServiceService extraService;

    @PostMapping("")
    public ApiResponse<ExtraServiceResponse> create(@RequestBody ExtraServiceCreationRequest request) {
        ExtraServiceResponse extraServiceResponse = extraService.create(request);

        return ApiResponse.<ExtraServiceResponse>builder().data(extraServiceResponse).build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ExtraServiceResponse> getById(@PathVariable Long id) {
        ExtraServiceResponse extraServiceResponse  = extraService.getByid(id);

        return ApiResponse.<ExtraServiceResponse>builder().data(extraServiceResponse).build();
    }

    @GetMapping
    public ApiResponse<List<ExtraServiceResponse>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String q,
            @RequestParam(defaultValue = "") ServiceType type
    ) {

        PageRequest pageRequest = PageRequest.of(
                page - 1,
                limit
        );

        Page<ExtraServiceResponse> response = extraService.getList(
                pageRequest,
                q,
               type
        );

        MetaPagination meta = MetaPagination.builder()
                .hasPrev(response.hasPrevious())
                .hasNext(response.hasNext())
                .limit(response.getSize())
                .page(page)
                .total(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .build();

        return ApiResponse.<List<ExtraServiceResponse>>builder()
                .data(response.getContent())
                .pagination(meta)
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRoomType(@PathVariable Long id) {
        extraService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Service Successfully")
                .build();
    }


    @PutMapping("/{id}")
    public ApiResponse<ExtraServiceResponse> updateRoomType(@RequestBody ExtraServiceUpdateRequest request, @PathVariable Long id) {
        ExtraServiceResponse response = extraService.update(id, request);

        return ApiResponse.<ExtraServiceResponse>builder().data(response).build();
    }
}
