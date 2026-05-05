package com.hotelmanagement.backend.dto.request;

import com.hotelmanagement.backend.entity.Permisson;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateRoleRequest {
    String name;
    String description;
    Set<String> permissions;
}
