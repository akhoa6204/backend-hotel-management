package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.ExtraServiceCreationRequest;
import com.hotelmanagement.backend.dto.request.ExtraServiceUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.ExtraServiceResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.enums.ServiceType;
import com.hotelmanagement.backend.mapper.ExtraServiceMapper;
import com.hotelmanagement.backend.service.ExtraServiceService;
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
@RequestMapping("/staff/extra-services")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class StaffExtraServiceController {
    ExtraServiceService extraService;
    ExtraServiceMapper extraServiceMapper;

    @PreAuthorize("hasAuthority('EXTRA_SERVICE_CREATE')")
    @PostMapping("")
    public ApiResponse<ExtraServiceResponse> create(@RequestBody @Valid ExtraServiceCreationRequest request) {
        ExtraServiceResponse extraServiceResponse = extraServiceMapper.toExtraServiceResponse(extraService.create(request));

        return ApiResponse.<ExtraServiceResponse>builder().data(extraServiceResponse).build();
    }

    @PreAuthorize("hasAuthority('EXTRA_SERVICE_READ')")
    @GetMapping("/{id}")
    public ApiResponse<ExtraServiceResponse> getById(@PathVariable Long id) {
        ExtraServiceResponse extraServiceResponse  = extraServiceMapper.toExtraServiceResponse(extraService.getByid(id));

        return ApiResponse.<ExtraServiceResponse>builder().data(extraServiceResponse).build();
    }

    @PreAuthorize("hasAuthority('EXTRA_SERVICE_READ')")
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
        ).map(extraServiceMapper::toExtraServiceResponse);

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

    @PreAuthorize("hasAuthority('EXTRA_SERVICE_DELETE')")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteById(@PathVariable Long id) {
        extraService.deleteById(id);
        return ApiResponse.<String>builder()
                .message("Delete Service Successfully")
                .build();
    }


    @PreAuthorize("hasAuthority('EXTRA_SERVICE_UPDATE')")
    @PutMapping("/{id}")
    public ApiResponse<ExtraServiceResponse> update(@RequestBody ExtraServiceUpdateRequest request, @PathVariable Long id) {
        ExtraServiceResponse response = extraServiceMapper.toExtraServiceResponse(extraService.update(id, request));

        return ApiResponse.<ExtraServiceResponse>builder().data(response).build();
    }
}
