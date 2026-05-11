package com.hotelmanagement.backend.entity;

import com.hotelmanagement.backend.enums.InvoiceItemType;
import com.hotelmanagement.backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "invoiceId", nullable = false)
    Invoice invoice;

    @Enumerated(EnumType.STRING)
    InvoiceItemType type;


    @ManyToOne
    @JoinColumn(name = "extraServiceId")
    ExtraService extraService;

    long quantity;

    @Column(precision = 10, scale = 2)
    BigDecimal unitPrice;

    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
