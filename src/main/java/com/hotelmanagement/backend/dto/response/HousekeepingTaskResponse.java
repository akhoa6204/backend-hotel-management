package com.hotelmanagement.backend.dto.response;

import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.HousekeepingTaskStatus;
import com.hotelmanagement.backend.enums.HousekeepingTaskType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
@Getter
@Setter
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class HousekeepingTaskResponse {
    Long id;
    RoomShortResponse room;
    UserShortResponse staff;
    String bookingId;
    Date createdAt;
    Date updatedAt;
    LocalDateTime startedAt;
    LocalDateTime completedAt;
    HousekeepingTaskType type;
    HousekeepingTaskStatus status;
    String note;
}
