package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Amenity;
import com.hotelmanagement.backend.entity.Promotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    boolean existsByCodeAndActiveTrue(String code);
    Optional<Promotion> findByIdAndActiveTrue(Long id);
    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.active = true
    
        AND (
            :q IS NULL
            OR :q = ''
            OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(p.code) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    Page<Promotion> getItemsWithParams(
            @Param("q") String q,
            Pageable pageable
    );

}
