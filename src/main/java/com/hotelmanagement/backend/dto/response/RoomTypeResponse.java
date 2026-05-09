package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.Amenity;
import com.hotelmanagement.backend.entity.RoomTypeImage;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class RoomTypeResponse {
    Long id;
    String name;
    String description;
    String capacity;
    BigDecimal basePrice;
    Set<RoomTypeImageResponse> roomTypeImages;
    Set<AmenityResponse> amenities;
}
