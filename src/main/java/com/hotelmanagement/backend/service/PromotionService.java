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
import java.util.Objects;
import java.util.Optional;
import java.math.BigDecimal;

import java.math.RoundingMode;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionService {
    PromotionRepository promotionRepository;
    PromotionMapper promotionMapper;

    public Promotion create(PromotionCreationRequest request) {
        if (promotionRepository.existsByCodeAndActiveTrue(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }

        Promotion promotion = promotionMapper.toPromotion(request);

        promotion.setActive(true);

        return promotionRepository.save(promotion);
    }

    public Page<Promotion> getList(
            PageRequest request,
            String q
    ) {
        return promotionRepository
                .getItemsWithParams(q, request);
    }

    public Promotion getById (Long id){
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        return promotion;
    }

    public void deleteById(Long id){
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        promotion.setActive(false);
        promotionRepository.save(promotion);
    }

    public Promotion update(Long id, PromotionUpdateRequest request) {
        Promotion promotion = promotionRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

        if (!Objects.equals(promotion.getCode(), request.getCode()) && promotionRepository.existsByCodeAndActiveTrue(request.getCode())) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }

        promotionMapper.updatePromotion(promotion, request);


        return promotionRepository.save(promotion);

    }
    public Promotion getValidManualPromotion(String code) {
        LocalDate today = LocalDate.now();

        Promotion promotion =promotionRepository.getItemWithParams(
                     code ,
                    false,
                    today
                ).stream().findFirst().orElseThrow(() -> new AppException(ErrorCode.PROMOTION_EXPIRED)
        );

        if (promotion.getQuotaUsed() >= promotion.getQuotaTotal()) {
            throw new AppException(ErrorCode.PROMOTION_QUOTA_EXCEEDED);
        }

        return promotion;
    }

    public Optional<Promotion> getValidAutoPromotion() {
        return promotionRepository.getItemWithParams(
                    null,
                    true,
                    LocalDate.now()
                ).stream().findFirst();
    }

    public void increaseQuota(Long id) {
        Promotion promotion = getById(id);
        promotion.setQuotaUsed(promotion.getQuotaUsed() + 1);

        promotionRepository.save(promotion);
    }

    public BigDecimal calculateAutoDiscountAmount(
            BigDecimal basePrice,
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (basePrice == null || startDate == null || endDate == null) {
            return BigDecimal.ZERO;
        }
        List<Promotion> promotions = getValidAutoPromotions(startDate, endDate);

        if (promotions.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;

        for (Promotion promotion : promotions) {
            BigDecimal discount = calculateDiscountByPromotion(basePrice, promotion);

            if (discount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            totalDiscount = totalDiscount.add(discount);

            if (!promotion.isStackable()) {
                break;
            }
        }

        if (totalDiscount.compareTo(basePrice) > 0) {
            return basePrice;
        }

        return totalDiscount;
    }

    public List<Promotion> getValidAutoPromotions(
            LocalDate startDate,
            LocalDate endDate
    ) {
        if (startDate == null || endDate == null) {
            return List.of();
        }

        return promotionRepository.findValidAutoPromotions(startDate, endDate);
    }

    private BigDecimal calculateDiscountByPromotion(
            BigDecimal basePrice,
            Promotion promotion
    ) {
        if (promotion == null || promotion.getDiscountValue() == null) {
            return BigDecimal.ZERO;
        }

        if (promotion.getQuotaTotal() > 0
                && promotion.getQuotaUsed() >= promotion.getQuotaTotal()) {
            return BigDecimal.ZERO;
        }

        if (promotion.getMinTotal() != null
                && basePrice.compareTo(promotion.getMinTotal()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = switch (promotion.getDiscountType()) {
            case PERCENTAGE -> basePrice
                    .multiply(promotion.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            case FIXED_AMOUNT -> promotion.getDiscountValue();
        };

        if (promotion.getMaxDiscountAmount() != null
                && discount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
            return promotion.getMaxDiscountAmount();
        }

        return discount;
    }

}
