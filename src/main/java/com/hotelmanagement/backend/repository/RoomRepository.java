package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Page<Room> findByActiveTrue(Pageable pageable);

    Optional<Room> findByIdAndActiveTrue(Long id);

    boolean existsByNameAndActiveTrue(String name);

    @Query("""
        SELECT r
        FROM Room r
        WHERE r.active = true
        AND (
            :q IS NULL
            OR :q = ''
            OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    Page<Room> getRoomsWithParams(
            @Param("q") String q,
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
