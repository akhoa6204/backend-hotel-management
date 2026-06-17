package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.ReviewStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "bookingId", nullable = false, unique = true)
    Booking booking;

    @Column(nullable = false)
    Integer overall;

    @Column(nullable = false)
    Integer amenities;

    @Column(nullable = false)
    Integer cleanliness;

    @Column(nullable = false)
    Integer comfort;

    @Column(nullable = false)
    Integer locationScore;

    @Column(nullable = false)
    Integer valueForMoney;

    @Column(nullable = false)
    Integer hygiene;

    @Column(columnDefinition = "TEXT")
    String comment;

    @Builder.Default
    Boolean active = true;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    ReviewStatus status = ReviewStatus.PUBLISHED;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
