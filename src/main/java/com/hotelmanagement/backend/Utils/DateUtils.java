package com.hotelmanagement.backend.Utils;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal=true)
    public class DateUtils {
        public static long computeNight(LocalDate startDate, LocalDate endDate) {
            return Math.max(1, ChronoUnit.DAYS.between(startDate, endDate));
        }
    }
