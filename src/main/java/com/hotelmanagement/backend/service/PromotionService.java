package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.PromotionCreationRequest;
import com.hotelmanagement.backend.dto.request.PromotionUpdateRequest;
import com.hotelmanagement.backend.dto.request.RoomCreationRequest;
import com.hotelmanagement.backend.dto.request.RoomUpdateRequest;
import com.hotelmanagement.backend.dto.response.PromotionResponse;
import com.hotelmanagement.backend.dto.response.RoomResponse;
import com.hotelmanagement.backend.entity.Promotion;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.entity.RoomType;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.enums.RoomStatus;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.PromotionMapper;
import com.hotelmanagement.backend.mapper.RoomMapper;
import com.hotelmanagement.backend.repository.PromotionRepository;
import com.hotelmanagement.backend.repository.RoomRepository;
import com.hotelmanagement.backend.repository.RoomTypeRepository;
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
public class PromotionService {
    PromotionRepository promotionRepository;

    PromotionMapper promotionMapper;

    public PromotionResponse create(PromotionCreationRequest request) {

        if (promotionRepository.existsByCodeAndActiveTrue(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }

        Promotion promotion = promotionMapper.toPromotion(request);

        promotion.setActive(true);

        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));
    }

    public Page<PromotionResponse> getList(
            PageRequest request,
            String q
    ) {

        return promotionRepository
                .getItemsWithParams(q, request)
                .map(promotionMapper::toPromotionResponse);
    }

    public PromotionResponse getById (Long id){
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        return promotionMapper.toPromotionResponse(promotion);
    }

    public void deleteById(Long id){
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    public PromotionResponse update(Long id, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        if (promotionRepository.existsByCodeAndActiveTrue(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }

        promotionMapper.updatePromotion(promotion, request);


        return promotionMapper.toPromotionResponse(promotionRepository.save(promotion));

    }


}
