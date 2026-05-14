package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.request.PaymentUpdateRequest;
import com.hotelmanagement.backend.dto.response.InvoiceItemResponse;
import com.hotelmanagement.backend.dto.response.PaymentResponse;
import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.Payment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    @Mapping(
            target = "invoiceId",
            source = "invoice.id"
    )
    @Mapping(
            target = "invoiceCode",
            source = "invoice.invoiceCode"
    )
    PaymentResponse toPaymentResponse(Payment payment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePayment(@MappingTarget Payment payment, PaymentUpdateRequest request );
}
