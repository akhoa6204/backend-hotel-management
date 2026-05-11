package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.Invoice;
import com.hotelmanagement.backend.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem,Long> {
}
