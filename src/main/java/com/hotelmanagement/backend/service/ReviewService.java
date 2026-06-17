package com.hotelmanagement.backend.service;

import com.hotelmanagement.backend.dto.request.ReviewCreationRequest;
import com.hotelmanagement.backend.dto.request.ReviewUpdateRequest;
import com.hotelmanagement.backend.dto.response.ReviewOverviewResponse;
import com.hotelmanagement.backend.dto.response.ReviewResponse;
import com.hotelmanagement.backend.dto.response.ReviewStatsResponse;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.Review;
import com.hotelmanagement.backend.enums.BookingStatus;
import com.hotelmanagement.backend.mapper.ReviewMapper;
import com.hotelmanagement.backend.repository.BookingRepository;
import com.hotelmanagement.backend.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {
    ReviewRepository reviewRepository;
    BookingRepository bookingRepository;
    ReviewMapper reviewMapper;

    public Page<ReviewResponse> getPublicReviewsByRoomType(String roomTypeId, Pageable pageable) {
        return reviewRepository.findPublicReviewsByRoomTypeId(roomTypeId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    public ReviewResponse createMyReview(String userId, String bookingId, ReviewCreationRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("BOOKING_NOT_FOUND"));

        if (booking.getCustomer() == null || !booking.getCustomer().getId().equals(userId)) {
            throw new RuntimeException("BOOKING_NOT_BELONG_TO_USER");
        }

        if (booking.getStatus() != BookingStatus.CHECKED_OUT) {
            throw new RuntimeException("BOOKING_NOT_CHECKED_OUT");
        }

        if (reviewRepository.existsByBookingId(bookingId)) {
            throw new RuntimeException("REVIEW_ALREADY_EXISTS");
        }

        Review review = reviewMapper.toReview(request);
        review.setBooking(booking);
        review.setActive(true);

        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    public Page<ReviewResponse> getMyReviews(String userId, Pageable pageable) {
        return reviewRepository.findByBookingCustomerId(userId, pageable)
                .map(reviewMapper::toReviewResponse);
    }

    public ReviewResponse getMyReviewById(String userId, String id) {
        Review review = reviewRepository.findByIdAndBookingCustomerId(id, userId)
                .orElseThrow(() -> new RuntimeException("REVIEW_NOT_FOUND"));
        return reviewMapper.toReviewResponse(review);
    }

    public Page<ReviewResponse> getList(Pageable pageable) {
        return reviewRepository.findAll(pageable)
                .map(reviewMapper::toReviewResponse);
    }

    public ReviewResponse updateActive(String id, ReviewUpdateRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("REVIEW_NOT_FOUND"));
        reviewMapper.updateReview(review, request);
        return reviewMapper.toReviewResponse(reviewRepository.save(review));
    }

    public ReviewOverviewResponse getPublicReviewOverviewByRoomType(String roomTypeId) {
        return ReviewOverviewResponse.builder()
                .avgOverall(reviewRepository.getPublicAverageByRoomTypeId(roomTypeId))
                .totalReviews(reviewRepository.countPublicReviewsByRoomTypeId(roomTypeId))
                .build();
    }

    public ReviewStatsResponse getStats() {
        return ReviewStatsResponse.builder()
                .avgOverall(reviewRepository.getActiveAverageOverall())
                .avgAmenities(reviewRepository.getActiveAverageAmenities())
                .avgCleanliness(reviewRepository.getActiveAverageCleanliness())
                .avgComfort(reviewRepository.getActiveAverageComfort())
                .avgLocationScore(reviewRepository.getActiveAverageLocationScore())
                .avgValueForMoney(reviewRepository.getActiveAverageValueForMoney())
                .avgHygiene(reviewRepository.getActiveAverageHygiene())
                .totalActiveReviews(reviewRepository.countByActiveTrue())
                .totalHiddenReviews(reviewRepository.countByActiveFalse())
                .build();
    }
}
