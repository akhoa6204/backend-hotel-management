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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion,Long> {
    boolean existsByCodeAndActiveTrue(String code);
    Optional<Promotion> findByIdAndActiveTrue(Long id);
    Optional<Promotion> findByAutoAppliedTrueAndActiveTrue();
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

    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.active = true
    
        AND (
            :promotionCode IS NULL
            OR LOWER(p.code) = LOWER(:promotionCode)
        )
    
        AND (
            :autoApplied IS NULL
            OR p.autoApplied = :autoApplied
        )
    
        AND (
            :today IS NULL
            OR (
                p.startDate <= :today
                AND p.endDate >= :today
            )
        )
        AND p.quotaUsed < p.quotaTotal
        ORDER BY p.priority DESC
    """)
    List<Promotion> getItemWithParams(
            @Param("promotionCode") String promotionCode,
            @Param("autoApplied") Boolean autoApplied,
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT p
        FROM Promotion p
        WHERE p.active = true
          AND p.autoApplied = true
          AND p.startDate <= :endDate
          AND p.endDate >= :startDate
          AND (p.quotaTotal = 0 OR p.quotaUsed < p.quotaTotal)
        ORDER BY p.priority ASC
    """)
    List<Promotion> findValidAutoPromotions(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
