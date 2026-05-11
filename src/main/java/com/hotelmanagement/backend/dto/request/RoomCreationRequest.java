package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoomCreationRequest {
    @NotBlank(message = "REQUIRED_FIELD")
    String name;

    @NotNull(message = "REQUIRED_FIELD")
    Long roomTypeId;
}
