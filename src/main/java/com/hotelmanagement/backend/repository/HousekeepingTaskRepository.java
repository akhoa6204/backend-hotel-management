package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Amenity;
import com.hotelmanagement.backend.entity.HousekeepingTask;
import com.hotelmanagement.backend.enums.HousekeepingTaskStatus;
import com.hotelmanagement.backend.enums.HousekeepingTaskType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask,Long> {


    @Query("""
            SELECT h
            FROM HousekeepingTask h
                JOIN h.room r
            WHERE
                (:status IS NULL OR h.status = :status)
                AND (
                    :q IS NULL
                    OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%'))
                )
                AND (
                    :bookingId IS NULL
                    OR h.bookingId = :bookingId
                )
           """)
    Page<HousekeepingTask> getItemsWithParams(
            @Param("status") HousekeepingTaskStatus status,
            @Param("q") String q,
            @Param("bookingId") String bookingId,
            Pageable pageable
    );

    boolean existsByRoomIdAndTypeAndStatusNot(
            Long roomId,
            HousekeepingTaskType type,
            HousekeepingTaskStatus status
    );

    boolean existsByRoomIdAndTypeAndBookingIdAndStatusNot(
            Long roomId,
            HousekeepingTaskType type,
            String bookingId,
            HousekeepingTaskStatus status
    );

    boolean existsByBookingIdAndTypeAndStatus(String bookingId, HousekeepingTaskType type, HousekeepingTaskStatus status);

    Optional<HousekeepingTask> findFirstByBookingIdAndType(String bookingId, HousekeepingTaskType type);

    long countByStaffIdAndStatusNot(
            String staffId,
            HousekeepingTaskStatus status
    );
}
