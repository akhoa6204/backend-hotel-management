package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.RoomType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class RoomResponse {
    Long id;
    String name;
    String status;
    RoomTypeResponse roomType;
}
