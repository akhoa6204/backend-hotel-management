package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.response.InvoiceResponse;
import com.hotelmanagement.backend.entity.Invoice;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Invoice toInvoice(InvoiceCreationData request);

    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "promotions", source = "invoicePromotions")
    InvoiceResponse toInvoiceResponse(Invoice invoice);
}
