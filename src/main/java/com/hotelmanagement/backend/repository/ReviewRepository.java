package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {

    boolean existsByBookingId(String bookingId);

    Optional<Review> findByIdAndBookingCustomerId(String id, String customerId);

    Page<Review> findByBookingCustomerId(String customerId, Pageable pageable);

    @Query("""
        SELECT r
        FROM Review r
        WHERE r.active = true
            AND r.booking.room.roomType.id = :roomTypeId
        ORDER BY r.createdAt DESC
    """)
    Page<Review> findPublicReviewsByRoomTypeId(@Param("roomTypeId") String roomTypeId, Pageable pageable);

    @Query("""
        SELECT COALESCE(AVG(r.overall), 0)
        FROM Review r
        WHERE r.active = true
            AND r.booking.room.roomType.id = :roomTypeId
    """)
    Double getPublicAverageByRoomTypeId(@Param("roomTypeId") String roomTypeId);

    @Query("""
        SELECT COUNT(r)
        FROM Review r
        WHERE r.active = true
            AND r.booking.room.roomType.id = :roomTypeId
    """)
    long countPublicReviewsByRoomTypeId(@Param("roomTypeId") String roomTypeId);

    @Query("""
        SELECT COALESCE(AVG(r.overall), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageOverall();

    @Query("""
        SELECT COALESCE(AVG(r.amenities), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageAmenities();

    @Query("""
        SELECT COALESCE(AVG(r.cleanliness), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageCleanliness();

    @Query("""
        SELECT COALESCE(AVG(r.comfort), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageComfort();

    @Query("""
        SELECT COALESCE(AVG(r.locationScore), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageLocationScore();

    @Query("""
        SELECT COALESCE(AVG(r.valueForMoney), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageValueForMoney();

    @Query("""
        SELECT COALESCE(AVG(r.hygiene), 0)
        FROM Review r
        WHERE r.active = true
    """)
    Double getActiveAverageHygiene();

    long countByActiveTrue();

    long countByActiveFalse();
}
