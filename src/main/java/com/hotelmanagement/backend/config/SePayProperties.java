package com.hotelmanagement.backend.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "sepay")
public class SePayProperties {
    private String bankAccount;
    private String bankCode;
}
