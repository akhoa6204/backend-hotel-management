package com.hotelmanagement.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreationRequest {

    @NotNull(message = "REQUIRED_FIELD")
    Integer overall;

    @NotNull(message = "REQUIRED_FIELD")
    Integer amenities;

    @NotNull(message = "REQUIRED_FIELD")
    Integer cleanliness;

    @NotNull(message = "REQUIRED_FIELD")
    Integer comfort;

    @NotNull(message = "REQUIRED_FIELD")
    Integer locationScore;

    @NotNull(message = "REQUIRED_FIELD")
    Integer valueForMoney;

    @NotNull(message = "REQUIRED_FIELD")
    Integer hygiene;

    @NotBlank(message = "REQUIRED_FIELD")
    String comment;
}
