package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Permisson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permisson, String> {
    boolean existsById(String id);
}
