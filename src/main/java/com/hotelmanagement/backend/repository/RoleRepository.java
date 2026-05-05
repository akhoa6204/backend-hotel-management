package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,String> {
    boolean existsById(String id);
}
