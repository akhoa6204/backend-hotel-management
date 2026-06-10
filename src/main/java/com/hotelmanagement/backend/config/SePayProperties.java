package com.hotelmanagement.backend.config;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePayProperties {
    String merchantId;
    String secretKey;
    String env;
}
