package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.ExtraServiceCreationRequest;
import com.hotelmanagement.backend.dto.request.ExtraServiceUpdateRequest;
import com.hotelmanagement.backend.dto.request.HousekeepingTaskCreationRequest;
import com.hotelmanagement.backend.dto.request.HousekeepingTaskUpdateRequest;
import com.hotelmanagement.backend.dto.response.BookingInspectionSocketResponse;
import com.hotelmanagement.backend.dto.response.HousekeepingTaskResponse;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.HousekeepingTask;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.User;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.HousekeepingTaskStatus;
import com.hotelmanagement.backend.enums.HousekeepingTaskType;
import com.hotelmanagement.backend.enums.ServiceType;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.ExtraServiceMapper;
import com.hotelmanagement.backend.mapper.HousekeepingTaskMapper;
import com.hotelmanagement.backend.repository.ExtraServiceRepository;
import com.hotelmanagement.backend.repository.HousekeepingTaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.hotelmanagement.backend.entity.StaffShiftAssignment;
import com.hotelmanagement.backend.enums.StaffPosition;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HousekeepingTaskService {
    HousekeepingTaskRepository housekeepingTaskRepository;
    StaffShiftAssignmentRepository staffShiftAssignmentRepository;
    HousekeepingTaskMapper housekeepingTaskMapper;

    RoomService roomService;
    UserService userService;

    SimpMessagingTemplate messagingTemplate;

    public HousekeepingTask createTask(HousekeepingTaskCreationRequest request) {

        HousekeepingTask housekeepingTask =
                housekeepingTaskMapper.toHousekeepingTask(request);

        Room room = roomService.getByid(request.getRoomId());

        boolean hasStaff = request.getStaffId() != null && !request.getStaffId().isBlank();
        User staff = hasStaff ? userService.getById(request.getStaffId()) : findAvailableHousekeepingStaff();

        housekeepingTask.setRoom(room);
        housekeepingTask.setStaff(staff);

        housekeepingTask.setStatus(HousekeepingTaskStatus.PENDING);

        boolean hasBookingId = request.getBookingId() != null && !request.getBookingId().isBlank();

        boolean existed = hasBookingId
                ? housekeepingTaskRepository.existsByRoomIdAndTypeAndBookingIdAndStatusNot(
                        request.getRoomId(),
                        request.getType(),
                        request.getBookingId(),
                        HousekeepingTaskStatus.COMPLETED
                )
                : housekeepingTaskRepository.existsByRoomIdAndTypeAndStatusNot(
                        request.getRoomId(),
                        request.getType(),
                        HousekeepingTaskStatus.COMPLETED
                );

        if (existed) {
            throw new AppException(
                    ErrorCode.HOUSEKEEPING_TASK_ALREADY_EXISTS
            );
        }

        return housekeepingTaskRepository.save(housekeepingTask);
    }

    private User findAvailableHousekeepingStaff() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        List<StaffShiftAssignment> assignments = staffShiftAssignmentRepository
                .findByWorkDateAndPositionAndShift_StartTimeLessThanEqualAndShift_EndTimeGreaterThanEqual(
                        today,
                        StaffPosition.HOUSEKEEPING,
                        now,
                        now
                );

        return assignments.stream()
                .map(StaffShiftAssignment::getStaff)
                .distinct()
                .min(Comparator.comparingLong(staff -> housekeepingTaskRepository
                        .countByStaffIdAndStatusNot(
                                staff.getId(),
                                HousekeepingTaskStatus.COMPLETED
                        )))
                .orElse(null);
    }

    public HousekeepingTask getByTaskId(Long id){
        return housekeepingTaskRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.HOUSEKEEPING_TASK_NOT_FOUND));
    }

    public Page<HousekeepingTask> getTaskList(PageRequest pageRequest, HousekeepingTaskStatus status, String q, String bookingId) {
        return housekeepingTaskRepository.getItemsWithParams(status, q, bookingId, pageRequest);
    }

    public HousekeepingTask updateTask(
            Long id, String userId, HousekeepingTaskUpdateRequest request) {

        HousekeepingTask housekeepingTask = getByTaskId(id);

        housekeepingTaskMapper.updateHousekeepingTask(request, housekeepingTask);

        LocalDateTime now = LocalDateTime.now();

        if (request.getStatus() == HousekeepingTaskStatus.IN_PROGRESS) {
            housekeepingTask.setStartedAt(now);
        } else if (request.getStatus() == HousekeepingTaskStatus.COMPLETED) {
            housekeepingTask.setCompletedAt(now);
        }

        if (housekeepingTask.getStaff() == null && request.getStaffId() == null) {
            housekeepingTask.setStaff(userService.getById(userId));
        }else if (request.getStaffId() != null) {
            housekeepingTask.setStaff(userService.getById(request.getStaffId()));
        }

        HousekeepingTask updated = housekeepingTaskRepository.save(housekeepingTask);

        if (
                updated.getStatus() == HousekeepingTaskStatus.COMPLETED
                        && updated.getType() == HousekeepingTaskType.INSPECTION
                        && updated.getBookingId() != null
        ) {
            BookingInspectionSocketResponse payload =
                    BookingInspectionSocketResponse.builder()
                            .bookingId(updated.getBookingId())
                            .inspectionTaskId(updated.getId())
                            .inspected(true)
                            .build();

            messagingTemplate.convertAndSend(
                    "/topic/bookings/" + updated.getBookingId() + "/inspection",
                    payload
            );
        }

        return updated;
    }

    public boolean hasCompletedInspectionTask(String bookingId){
        return housekeepingTaskRepository.existsByBookingIdAndTypeAndStatus(bookingId, HousekeepingTaskType.INSPECTION, HousekeepingTaskStatus.COMPLETED);
    }

    public Optional<HousekeepingTask> findInspectionTaskByBookingId(String bookingId){
        return housekeepingTaskRepository.findFirstByBookingIdAndType(bookingId, HousekeepingTaskType.INSPECTION);
    }
}
