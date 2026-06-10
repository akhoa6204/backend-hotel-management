package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.HousekeepingTaskStatus;
import com.hotelmanagement.backend.enums.HousekeepingTaskType;
import com.hotelmanagement.backend.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousekeepingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "roomId")
    Room room;

    @ManyToOne
    @JoinColumn(name = "staffId")
    User staff;

    String bookingId;

    LocalDateTime startedAt;

    LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    HousekeepingTaskType type;

    @Enumerated(EnumType.STRING)
    HousekeepingTaskStatus status;

    String note;

    @CreationTimestamp
    Date createdAt;

    @UpdateTimestamp
    Date updatedAt;
}
