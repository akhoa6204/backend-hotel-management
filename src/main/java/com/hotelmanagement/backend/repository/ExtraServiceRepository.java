package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.enums.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExtraServiceRepository extends JpaRepository<ExtraService, Long> {
    @Query("""
        SELECT s
        FROM ExtraService s
        WHERE s.active = true
        AND (
            :type IS NULL
            OR s.type = :type
        )
        AND (
            :q IS NULL
            OR :q = ''
            OR s.name LIKE CONCAT('%', :q, '%')
        )
    """)
    Page<ExtraService> getServices(
            @Param("q") String q,
            @Param("type") ServiceType type,
            Pageable pageable
    );

    Optional<ExtraService> findByIdAndActiveTrue(Long id);

    boolean existsByNameAndActiveTrue(String name);


}
