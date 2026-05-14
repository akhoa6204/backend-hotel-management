package com.hotelmanagement.backend.mapper;

import com.hotelmanagement.backend.dto.internal.InvoiceCreationData;
import com.hotelmanagement.backend.dto.response.InvoiceItemResponse;
import com.hotelmanagement.backend.dto.response.InvoiceResponse;
import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface InvoiceItemMapper {
    InvoiceItemResponse toInvoiceItemResponse(InvoiceItem item);
}
