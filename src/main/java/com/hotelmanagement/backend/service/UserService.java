package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.*;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.entity.Role;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.mapper.UserMapper;
import com.hotelmanagement.backend.repository.RoleRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;

    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        };

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role role = roleRepository.findById(UserRole.USER.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setRole(role);
        return userRepository.save(user);
    }

    public Page<User> getEmployees(PageRequest pageRequest, UserRole role) {
        if (role == null) {
            return userRepository.findEmployees(pageRequest);
        }

        if (role == UserRole.USER) {
            return Page.empty(pageRequest);
        }

        return userRepository.findEmployeesByRole(role.name(), pageRequest);
    }

    public User getById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public User updateUser(String userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        userMapper.updateUser(user, request);
        if(request.getPassword()!=null){
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if(request.getRole() != null) {
            Role role = roleRepository.findById(request.getRole()).orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.setRole(role);
        }


        return userRepository.save(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }

    public User createEmployee(EmployeeCreationRequest request) {
        User user = userMapper.employeeToUser(request);

        String defaultPassword = "123";
        user.setPassword(passwordEncoder.encode(defaultPassword));

        Role role = roleRepository.findById(String.valueOf(request.getRole()))
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setRole(role);
        user.setActive(true);
        return userRepository.save(user);
    }

    public void resetPassword(String userId, EmployeeResetPasswordRequest request) {
        User user = getById(userId);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    };

    public User updateEmployee(String userId, EmployeeUpdateRequest request) {
        User user = getById(userId);

        if(request.getRole() != null) {
            Role role = roleRepository.findById(String.valueOf(request.getRole()))
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
            user.setRole(role);
        }

        userMapper.updateEmployee(user, request);

        return userRepository.save(user);
    }
}
