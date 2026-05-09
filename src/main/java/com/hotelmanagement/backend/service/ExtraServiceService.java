package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.ExtraServiceUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.request.ExtraServiceCreationRequest;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.dto.response.ExtraServiceResponse;
import com.hotelmanagement.backend.entity.ExtraService;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.RoomType;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.RoomStatus;
import com.hotelmanagement.backend.enums.ServiceType;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.ExtraServiceMapper;
import com.hotelmanagement.backend.repository.ExtraServiceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExtraServiceService {
    ExtraServiceRepository extraServiceRepository;

    ExtraServiceMapper extraServiceMapper;

    public ExtraServiceResponse create(ExtraServiceCreationRequest request) {

        if (extraServiceRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new AppException(ErrorCode.SERVICE_ALREADY_EXISTS);
        }

        ExtraService extraService = extraServiceMapper.toExtraService(request);

        extraService.setActive(true);

        return extraServiceMapper.toExtraServiceResponse(extraServiceRepository.save(extraService));
    }

    public Page<ExtraServiceResponse> getList(
            PageRequest request,
            String q,
            ServiceType type){
        return extraServiceRepository.getServices(q, type, request).map(extraServiceMapper::toExtraServiceResponse);
    }

    public ExtraServiceResponse getByid (Long id){
        ExtraService extraService = extraServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
        return extraServiceMapper.toExtraServiceResponse(extraService);
    }

    public void deleteById(Long id){
        ExtraService extraService = extraServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        extraService.setActive(false);
        extraServiceRepository.save(extraService);
    }

    public ExtraServiceResponse update(Long id, ExtraServiceUpdateRequest request) {
        ExtraService extraService = extraServiceRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));

        if (extraServiceRepository.existsByNameAndActiveTrue(request.getName())) {
            throw new AppException(ErrorCode.SERVICE_ALREADY_EXISTS);
        }

        extraServiceMapper.updateService(extraService, request);


        return extraServiceMapper.toExtraServiceResponse(extraServiceRepository.save(extraService));

    }


}
