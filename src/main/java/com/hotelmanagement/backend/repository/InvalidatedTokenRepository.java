package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.InvalidatedToken;
import com.hotelmanagement.backend.entity.Permisson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    boolean existsById(String id);
}
