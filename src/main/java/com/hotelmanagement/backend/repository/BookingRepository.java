package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

@Repository
public interface BookingRepository extends JpaRepository<Booking,String> {
    @Query("""
        SELECT COUNT(b) > 0
        FROM Booking b
        WHERE
            b.room.id = :roomId
            AND b.status NOT IN ('CANCELLED', 'NO_SHOW')
            AND (
                :checkInDate < b.checkOutDate
                AND :checkOutDate > b.checkInDate
            )
    """)
    boolean existsBookingOverlap(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate
    );

    @Query("""
    SELECT b
    FROM Booking b
    WHERE
        b.status <> 'NO_SHOW'
        AND (
            :q IS NULL
            OR :q = ''
            OR LOWER(b.bookingCode)
                LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.guestName)
                LIKE LOWER(CONCAT('%', :q, '%'))
        )
""")
    Page<Booking> getItemsWithParams(
            @Param("q") String q,
            Pageable pageable
    );

    Optional<Booking> findByIdAndStatusNot(
            String id,
            BookingStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id and b.status <> :excludedStatus")
    Optional<Booking> findByIdForConfirmation(
            @Param("id") String id,
            @Param("excludedStatus") BookingStatus excludedStatus
    );

    @EntityGraph(attributePaths = {
            "customer",
            "room.roomType.roomTypeImages",
            "invoice.invoiceItems.extraService",
            "invoice.invoicePromotions",
            "invoice.payments"
    })
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findConfirmationEmailDetailById(@Param("id") String id);

    @EntityGraph(attributePaths = {
            "invoice.invoiceItems",
            "invoice.payments"
    })
    Optional<Booking> findDetailByIdAndStatusNot(
            String id,
            BookingStatus status
    );
    @Query("""
        SELECT b
        FROM Booking b
        WHERE
            b.status = 'CONFIRMED'
            AND b.checkOutDate <= :today
            AND EXISTS (
                SELECT p
                FROM Payment p
                WHERE
                    p.invoice.booking.id = b.id
                    AND p.status = 'SUCCESS'
                    AND (
                        p.type = 'DEPOSIT'
                        OR p.type = 'FULL_PAYMENT'
                        OR p.type = 'ROOM_PAYMENT'
                    )
            )
    """)
    List<Booking> findPaidNoShowBookings(
            @Param("today") LocalDate today
    );

    @Query("""
        SELECT b
        FROM Booking b
        WHERE
            b.status = 'PENDING'
            AND b.createdAt <= :expiredAt
            AND NOT EXISTS (
                SELECT p
                FROM Payment p
                WHERE
                    p.invoice.booking.id = b.id
                    AND p.status = 'SUCCESS'
            )
    """)
    List<Booking> findNoPaidNoShowBookings(
            @Param("expiredAt") LocalDateTime expiredAt
    );

    @Query("""
        select count(b)
        from Booking b
        where b.checkInDate = :date
            AND b.status <> 'NO_SHOW'
    """)
    long countTodayBookings(LocalDate date);

    Page<Booking> findByCheckInDateAndStatusNot(
            LocalDate date,
            BookingStatus status,
            Pageable pageable
    );

    Page<Booking> findByCheckOutDateAndStatusNot(LocalDate date, BookingStatus status, Pageable pageable);

    @Query("""
        SELECT COUNT(DISTINCT b.room.id)
        FROM Booking b
        WHERE b.checkInDate <= :endDate
            AND b.checkOutDate >= :startDate
            AND b.status NOT IN (
                com.hotelmanagement.backend.enums.BookingStatus.CANCELLED,
                com.hotelmanagement.backend.enums.BookingStatus.CHECKED_OUT,
                com.hotelmanagement.backend.enums.BookingStatus.NO_SHOW
            )
    """)
    long countOccupiedRoomsBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    long countByCheckInDateGreaterThanEqualAndCheckInDateLessThan(
            LocalDate startDate,
            LocalDate endDate
    );

    long countByCheckInDateGreaterThanEqualAndCheckInDateLessThanAndStatusIn(
            LocalDate startDate,
            LocalDate endDate,
            List<BookingStatus> statuses
    );

    long countByCheckInDateGreaterThanEqualAndCheckInDateLessThanAndStatus(
            LocalDate startDate,
            LocalDate endDate,
            BookingStatus status
    );

    @Query("""
    SELECT b
    FROM Booking b
    WHERE
        b.customer.id = :customerId
        AND b.status <> 'NO_SHOW'
        AND (
            :q IS NULL
            OR :q = ''
            OR LOWER(b.bookingCode) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.guestName) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.guestPhone) LIKE LOWER(CONCAT('%', :q, '%'))
            OR LOWER(b.guestEmail) LIKE LOWER(CONCAT('%', :q, '%'))
        )
    """)
    Page<Booking> getMyItemsWithParams(
            @Param("customerId") String customerId,
            @Param("q") String q,
            Pageable pageable
    );
}
