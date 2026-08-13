package com.hotelmanagement.backend.controller.staff;

import com.hotelmanagement.backend.dto.request.CloudinarySignatureRequest;
import com.hotelmanagement.backend.dto.request.CloudinaryDeleteRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.CloudinarySignatureResponse;
import com.hotelmanagement.backend.service.CloudinaryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/staff/uploads")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StaffUploadController {
    CloudinaryService cloudinaryService;

    @PostMapping("/cloudinary/signature")
    @PreAuthorize("hasAnyAuthority('ROOM_TYPE_CREATE', 'ROOM_TYPE_UPDATE')")
    public ApiResponse<CloudinarySignatureResponse> cloudinarySignature(
            @RequestBody @Valid CloudinarySignatureRequest request
    ) {
        return ApiResponse.<CloudinarySignatureResponse>builder()
                .data(cloudinaryService.generateUploadSignature(request))
                .build();
    }

    @DeleteMapping("/cloudinary")
    @PreAuthorize("hasAnyAuthority('ROOM_TYPE_CREATE', 'ROOM_TYPE_UPDATE')")
    public ApiResponse<String> deleteCloudinaryImage(
            @RequestBody @Valid CloudinaryDeleteRequest request
    ) {
        cloudinaryService.deleteImage(request);
        return ApiResponse.<String>builder()
                .message("Delete image successfully")
                .build();
    }
}
