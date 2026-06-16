package com.hotelmanagement.backend.controller.guest;

import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.ApiResponse;
import com.hotelmanagement.backend.dto.response.MetaPagination;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.mapper.RoomMapper;
import com.hotelmanagement.backend.service.RoomService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/public/rooms")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class PublicRoomController {
    RoomService roomService;
    RoomMapper roomMapper;

    @GetMapping("/{id}")
    public ApiResponse<RoomResponse> getById(@PathVariable Long id) {
        RoomResponse roomResponse =roomMapper.toRoomResponse (roomService.getByid(id));

        return ApiResponse.<RoomResponse>builder().data(roomResponse).build();
    }
}
