package com.hotelmanagement.backend.repository;

import com.hotelmanagement.backend.entity.InvoiceItem;
import com.hotelmanagement.backend.entity.InvoicePromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvoicePromotionRepository extends JpaRepository<InvoicePromotion,Long> {
}
