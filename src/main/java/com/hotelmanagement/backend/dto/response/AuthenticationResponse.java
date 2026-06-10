package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
public class AuthenticationResponse {
    boolean authenticated;
    String token;
    UserShortResponse user;
}
