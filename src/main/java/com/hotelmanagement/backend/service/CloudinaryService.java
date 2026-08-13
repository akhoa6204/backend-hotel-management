package com.hotelmanagement.backend.service;

import com.cloudinary.Cloudinary;
import com.hotelmanagement.backend.config.CloudinaryProperties;
import com.hotelmanagement.backend.dto.request.CloudinaryDeleteRequest;
import com.hotelmanagement.backend.dto.request.CloudinarySignatureRequest;
import com.hotelmanagement.backend.dto.response.CloudinarySignatureResponse;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTEXTS = Set.of("room-types");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    Cloudinary cloudinary;
    CloudinaryProperties properties;

    public CloudinarySignatureResponse generateUploadSignature(
            CloudinarySignatureRequest request
    ) {
        validateConfiguration();
        validateUploadRequest(request);

        String context = request.getContext();
        if (!ALLOWED_CONTEXTS.contains(context)) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_CONTEXT);
        }

        long timestamp = Instant.now().getEpochSecond();
        String folder = properties.getRootFolder() + "/" + context;
        Map<String, Object> params = Map.of(
                "timestamp", timestamp,
                "folder", folder
        );

        String signature = cloudinary.apiSignRequest(
                params,
                properties.getApiSecret()
        );

        return CloudinarySignatureResponse.builder()
                .timestamp(timestamp)
                .signature(signature)
                .apiKey(properties.getApiKey())
                .cloudName(properties.getCloudName())
                .folder(folder)
                .uploadUrl("https://api.cloudinary.com/v1_1/"
                        + properties.getCloudName()
                        + "/image/upload")
                .build();
    }

    public void deleteImageQuietly(String publicId) {
        if (isBlank(publicId)) {
            return;
        }

        try {
            validateConfiguration();
            cloudinary.uploader().destroy(publicId, Map.of());
        } catch (Exception exception) {
            // Cloudinary cleanup is best-effort after DB success.
            // The business update should not be reported as failed.
            log.warn(
                    "Cloudinary cleanup failed for publicId={}",
                    publicId,
                    exception
            );
        }
    }

    public void deleteImage(CloudinaryDeleteRequest request) {
        validateConfiguration();
        validatePublicIdContext(request.getPublicId(), request.getContext());

        try {
            cloudinary.uploader().destroy(request.getPublicId(), Map.of());
        } catch (Exception exception) {
            log.error("Cloudinary deletion failed for publicId={}", request.getPublicId(), exception);
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    public void validateImageMetadata(String secureUrl, String publicId, String context) {
        if (isBlank(secureUrl)) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_FILE);
        }

        if (isBlank(publicId)) {
            return;
        }

        validateConfiguration();
        validatePublicIdContext(publicId, context);

        try {
            URI imageUri = URI.create(secureUrl);
            String expectedPathPrefix = "/" + properties.getCloudName() + "/image/upload/";
            if (!"https".equalsIgnoreCase(imageUri.getScheme())
                    || !"res.cloudinary.com".equalsIgnoreCase(imageUri.getHost())
                    || imageUri.getPath() == null
                    || !imageUri.getPath().startsWith(expectedPathPrefix)) {
                throw new AppException(ErrorCode.UPLOAD_INVALID_FILE);
            }
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_FILE);
        }
    }

    private void validateConfiguration() {
        if (isBlank(properties.getCloudName())
                || isBlank(properties.getApiKey())
                || isBlank(properties.getApiSecret())) {
            throw new AppException(ErrorCode.UPLOAD_CONFIGURATION_INVALID);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void validateUploadRequest(CloudinarySignatureRequest request) {
        if (!ALLOWED_CONTENT_TYPES.contains(request.getContentType())
                || request.getSize() == null
                || request.getSize() <= 0
                || request.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_FILE);
        }

        String normalizedFileName = request.getFileName().toLowerCase(Locale.ROOT);
        if (!normalizedFileName.endsWith(".jpg")
                && !normalizedFileName.endsWith(".jpeg")
                && !normalizedFileName.endsWith(".png")
                && !normalizedFileName.endsWith(".webp")) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_FILE);
        }
    }

    private void validatePublicIdContext(String publicId, String context) {
        if (!ALLOWED_CONTEXTS.contains(context)) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_CONTEXT);
        }

        String expectedPrefix = properties.getRootFolder() + "/" + context + "/";
        if (publicId == null || !publicId.startsWith(expectedPrefix)) {
            throw new AppException(ErrorCode.UPLOAD_INVALID_CONTEXT);
        }
    }
}
