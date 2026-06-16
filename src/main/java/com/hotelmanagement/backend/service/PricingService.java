package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.Utils.DateUtils;
import com.hotelmanagement.backend.dto.response.PricingResultResponse;
import com.hotelmanagement.backend.dto.request.QuoteRequest;
import com.hotelmanagement.backend.entity.Promotion;
import com.hotelmanagement.backend.entity.Room;
import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.mapper.PromotionMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PricingService {
    PromotionService promotionService;
    RoomService roomService;
    PromotionMapper promotionMapper;
    public PricingResultResponse calculateBookingPrice(
            QuoteRequest request
    ) {
        Room room = roomService.findRoomAvailable(
                request.getRoomId(),
                request.getStartDate(),
                request.getEndDate());
        long nights = DateUtils.computeNight(request.getStartDate(), request.getEndDate());
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
        }

        if (autoPromotion != null) {
            autoPromotionDiscount = calculateDiscount(autoPromotion, subtotal);
            totalDiscount = totalDiscount.add(autoPromotionDiscount);
        }

        totalDiscount = totalDiscount.min(subtotal);
        BigDecimal finalTotal = subtotal.subtract(totalDiscount);

        return PricingResultResponse.builder()
                .nights(nights)
                .basePrice(basePrice)
                .subtotal(subtotal)
                .autoDiscount(autoPromotionDiscount)
                .totalDiscount(totalDiscount)
                .finalTotal(finalTotal)
                .promotionDiscount(promotionDiscount)
                .autoPromotion(promotionMapper.toPromotionResponse(autoPromotion))
                .promotion(promotionMapper.toPromotionResponse(promotion))
                .build();
    }

    private BigDecimal calculateDiscount(Promotion promotion, BigDecimal subtotal) {
        BigDecimal discount;

        if (promotion.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = subtotal.multiply(
                    promotion.getDiscountValue().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
            );
        } else {
            discount = promotion.getDiscountValue();
        }

        if (promotion.getMaxDiscountAmount() != null
                && promotion.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            discount = discount.min(promotion.getMaxDiscountAmount());
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

}
