package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.Utils.DateUtils;
import com.hotelmanagement.backend.dto.internal.PricingResult;
import com.hotelmanagement.backend.dto.request.BookingCreationRequest;
import com.hotelmanagement.backend.dto.request.CreateRoleRequest;
import com.hotelmanagement.backend.dto.response.RoleResponse;
import com.hotelmanagement.backend.entity.Permisson;
import com.hotelmanagement.backend.entity.Promotion;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.enums.ErrorCode;
import com.hotelmanagement.backend.exception.AppException;
import com.hotelmanagement.backend.mapper.RoleMapper;
import com.hotelmanagement.backend.repository.PermissionRepository;
import com.hotelmanagement.backend.repository.PromotionRepository;
import com.hotelmanagement.backend.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricingService {
    PromotionService promotionService;

    public PricingResult calculateBookingPrice(
            BookingCreationRequest request,
            Room room
    ) {

        long nights = DateUtils.computeNight(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal basePrice = room.getRoomType().getBasePrice();
        BigDecimal subtotal = basePrice.multiply(BigDecimal.valueOf(nights));
        Promotion promotion = null;

        if (request.getPromotionCode() != null && !request.getPromotionCode().isBlank()) {
            promotion = promotionService.getValidManualPromotion(request.getPromotionCode());
        }

        Promotion autoPromotion = null;

        if (promotion == null || promotion.isStackable()) {
            autoPromotion = promotionService.getValidAutoPromotion().orElse(null);
        }

        BigDecimal promotionDiscount = BigDecimal.ZERO;
        BigDecimal autoPromotionDiscount = BigDecimal.ZERO;
        BigDecimal totalDiscount = BigDecimal.ZERO;

        if (promotion != null) {
            promotionDiscount = calculateDiscount(promotion, subtotal);
            totalDiscount = totalDiscount.add(promotionDiscount);
            promotionService.increaseQuota(promotion);
        }

        if (autoPromotion != null) {
            autoPromotionDiscount = calculateDiscount(autoPromotion, subtotal);
            totalDiscount = totalDiscount.add(autoPromotionDiscount);
            promotionService.increaseQuota(autoPromotion);
        }

        totalDiscount = totalDiscount.min(subtotal);
        BigDecimal finalTotal = subtotal.subtract(totalDiscount);

        return PricingResult.builder()
                .nights(nights)
                .subtotal(subtotal)
                .autoDiscount(autoPromotionDiscount)
                .totalDiscount(totalDiscount)
                .finalTotal(finalTotal)
                .promotionDiscount(promotionDiscount)
                .autoPromotion(autoPromotion)
                .promotion(promotion)
                .build();
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal discount;

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(
                    promotion.getDiscountValue().divide(BigDecimal.valueOf(100))
                    );
        } else {
            discount = promotion.getDiscountValue();
        }

        if (promotion.getMaxDiscountAmount() > 0) {
            discount = discount.min(
                    BigDecimal.valueOf(promotion.getMaxDiscountAmount())
            );

        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

}
