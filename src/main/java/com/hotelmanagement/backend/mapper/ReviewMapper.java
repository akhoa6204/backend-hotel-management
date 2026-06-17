package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.ReviewCreationRequest;
import com.hotelmanagement.backend.dto.request.ReviewUpdateRequest;
import com.hotelmanagement.backend.dto.response.ReviewResponse;
import com.hotelmanagement.backend.dto.response.UserShortResponse;
import com.hotelmanagement.backend.entity.Review;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @org.mapstruct.Mapping(target = "bookingId", source = "booking.id")
    ReviewResponse toReviewResponse(Review review);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReview(@MappingTarget Review review, ReviewUpdateRequest reviewUpdateRequest);

    Review toReview(ReviewCreationRequest request);
}
