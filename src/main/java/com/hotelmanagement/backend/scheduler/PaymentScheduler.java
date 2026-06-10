package com.hotelmanagement.backend.scheduler;

import com.hotelmanagement.backend.entity.Payment;
import com.hotelmanagement.backend.enums.PaymentStatus;
import com.hotelmanagement.backend.repository.PaymentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentScheduler {
    PaymentRepository paymentRepository;
    @Scheduled(
            cron = "0 */5 * * * *",
            zone = "Asia/Ho_Chi_Minh"
    )
    @Transactional
    public void updateFailedPayments(){
        LocalDateTime now = LocalDateTime.now();
        List<Payment> payments = paymentRepository.findExpiredPendingPayments(now);

        for (Payment payment : payments) {
            payment.setStatus(PaymentStatus.FAILED);
        }

        paymentRepository.saveAll(payments);
    }
}
