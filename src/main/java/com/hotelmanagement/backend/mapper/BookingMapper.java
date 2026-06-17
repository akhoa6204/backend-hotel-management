package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.internal.BookingCreationData;
import com.hotelmanagement.backend.dto.request.BookingCreationRequest;
import com.hotelmanagement.backend.dto.request.BookingUpdateRequest;
import com.hotelmanagement.backend.dto.request.UserCreationRequest;
import com.hotelmanagement.backend.dto.request.UserUpdateRequest;
import com.hotelmanagement.backend.dto.response.BookingResponse;
import com.hotelmanagement.backend.dto.response.UserResponse;
import com.hotelmanagement.backend.entity.Booking;
import com.hotelmanagement.backend.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    Booking toBooking(BookingCreationData request);

    @Mapping(
            target = "invoiceId",
            source = "invoice.id"
    )
    @Mapping(target = "hasReview", expression = "java(booking.getReview() != null)")
    BookingResponse toBookingResponse(Booking booking);
}
