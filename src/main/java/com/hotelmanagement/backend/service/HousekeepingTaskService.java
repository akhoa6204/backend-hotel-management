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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HousekeepingTaskService {
    HousekeepingTaskRepository housekeepingTaskRepository;
    HousekeepingTaskMapper housekeepingTaskMapper;

    RoomService roomService;
    UserService userService;

    SimpMessagingTemplate messagingTemplate;

    public HousekeepingTask createTask(HousekeepingTaskCreationRequest request) {

        HousekeepingTask housekeepingTask =
                housekeepingTaskMapper.toHousekeepingTask(request);

        Room room = roomService.getByid(request.getRoomId());

        boolean hasStaff = request.getStaffId() != null;
        User staff = hasStaff ? userService.getById(request.getStaffId()) : null;

        housekeepingTask.setRoom(room);
        housekeepingTask.setStaff(staff);

        housekeepingTask.setStatus(HousekeepingTaskStatus.PENDING);

        boolean existed = housekeepingTaskRepository
                .existsByRoomIdAndTypeAndStatusNot(
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
