package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType,Long> {
    Page<RoomType> findByNameContainingIgnoreCaseAndActiveTrue(String q, Pageable pageable);

    Optional<RoomType> findByIdAndActiveTrue(Long id);

    boolean existsByNameAndActiveTrue(String name);
}
