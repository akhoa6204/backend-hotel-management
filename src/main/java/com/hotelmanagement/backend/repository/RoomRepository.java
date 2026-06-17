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
import java.util.Collection;
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
        AND (
            :roomTypeId IS NULL
            OR r.roomType.id = :roomTypeId
        )
        AND NOT EXISTS (
                SELECT b.id
                FROM Booking b
                WHERE b.room.id = r.id
                    AND b.checkInDate < :endDate
                    AND b.checkOutDate > :startDate
                    AND b.status NOT IN (
                        com.hotelmanagement.backend.enums.BookingStatus.CANCELLED,
                        com.hotelmanagement.backend.enums.BookingStatus.CHECKED_OUT,
                        com.hotelmanagement.backend.enums.BookingStatus.NO_SHOW
                    )
            )
    """)
    Page<Room> getRoomsWithParams(
            @Param("q") String q,
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
        SELECT r
        FROM Room r
        WHERE r.id = :id
            AND r.active = true
            AND NOT EXISTS (
                SELECT b.id
                FROM Booking b
                WHERE b.room.id = r.id
                    AND b.checkInDate < :endDate
                    AND b.checkOutDate > :startDate
                    AND b.status NOT IN (
                        com.hotelmanagement.backend.enums.BookingStatus.CANCELLED,
                        com.hotelmanagement.backend.enums.BookingStatus.CHECKED_OUT,
                        com.hotelmanagement.backend.enums.BookingStatus.NO_SHOW
                    )
            )
       """)
    Optional<Room> roomAvailable(
            @Param("id") Long id,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT COUNT(r)
        FROM Room r
        WHERE r.active = true
            AND NOT EXISTS (
                SELECT b.id
                FROM Booking b
                WHERE b.room.id = r.id
                    AND b.checkInDate < :endDate
                    AND b.checkOutDate > :startDate
                    AND b.status NOT IN (
                        com.hotelmanagement.backend.enums.BookingStatus.CANCELLED,
                        com.hotelmanagement.backend.enums.BookingStatus.CHECKED_OUT,
                        com.hotelmanagement.backend.enums.BookingStatus.NO_SHOW
                    )
            )
    """)
    long countAvailableRoomsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT r.id
        FROM room r
        JOIN roomType rt ON rt.id = r.roomTypeId
        WHERE rt.id = :roomTypeId
          AND rt.active = true
          AND rt.capacity >= :capacity
          AND NOT EXISTS (
              SELECT 1
              FROM booking b
              WHERE b.roomId = r.id
                AND b.status IN (:bookingStatuses)
                AND b.checkInDate < :endDate
                AND b.checkOutDate > :startDate
          )
        ORDER BY r.id ASC
        LIMIT 1
    """, nativeQuery = true)
    Optional<Long> findFirstAvailableRoomIdByRoomType(
            @Param("roomTypeId") Long roomTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("capacity") Integer capacity,
            @Param("bookingStatuses") Collection<String> bookingStatuses
    );
}
