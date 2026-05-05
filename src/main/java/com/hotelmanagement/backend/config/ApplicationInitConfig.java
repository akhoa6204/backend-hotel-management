package com.hotelmanagement.backend.config;

import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.repository.RoleRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_EMAIL = "admin@diamondsea.hotel.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(UserRepository userRepository, RoleRepository roleRepository) {
        return args -> {
            if(userRepository.findByEmail(ADMIN_EMAIL).isEmpty()){
                roleRepository.save(Role.builder()
                        .name(com.hotelmanagement.backend.enums.Role.USER.name())
                        .description("User role")
                        .build());

                Role adminRole = roleRepository.save(Role.builder()
                        .name(com.hotelmanagement.backend.enums.Role.ADMIN.name())
                        .description("Admin role")
                        .build());

                var roles = new HashSet<Role>();
                roles.add(adminRole);

                User user = User.builder()
                        .fullName("admin")
                        .email(ADMIN_EMAIL)
                        .roles(roles)
                        .password(passwordEncoder.encode(ADMIN_PASSWORD))
                        .build();

                userRepository.save(user);
                log.warn("Admin has been created with default password: admin, please change it");
            };
        };
    };
}
