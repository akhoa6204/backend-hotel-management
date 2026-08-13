package com.hotelmanagement.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinarySignatureResponse {
    Long timestamp;
    String signature;
    String apiKey;
    String cloudName;
    String folder;
    String uploadUrl;
}
