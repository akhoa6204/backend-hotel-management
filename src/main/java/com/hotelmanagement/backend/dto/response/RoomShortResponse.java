package com.hotelmanagement.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class RoomShortResponse {
    Long id;
    String name;
    RoomTypeShortResponse roomType;
}
