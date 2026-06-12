package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.StaffPosition;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class StaffShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staffId", nullable = false)
    User staff;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shiftId", nullable = false)
    Shift shift;

    @Column(name = "workDate", nullable = false)
    LocalDate workDate;

    @Enumerated(EnumType.STRING)

    @Column(nullable = false)
    StaffPosition position;

    @CreationTimestamp

    @Column()
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column()
    private LocalDateTime updatedAt;

}
