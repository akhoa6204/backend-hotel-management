package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.UserCreationRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
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
        HashSet<Role> roles = new HashSet<>();

        Role role = roleRepository.findById(UserRole.USER.name())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        roles.add(role);

        user.setRoles(roles);
        return userRepository.save(user);
    }

    public Page<User> getUsers(PageRequest pageRequest) {
        return userRepository
                .findAll(pageRequest);
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
        if(!CollectionUtils.isEmpty(request.getRoles())) {
            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }


        return userRepository.save(user);
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
    }
}
