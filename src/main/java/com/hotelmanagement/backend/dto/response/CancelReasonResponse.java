package com.hotelmanagement.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancelReasonResponse {
    String id;
    String bookingId;
    String reason;
    boolean staffCancel;
    UserShortResponse cancelledBy;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
