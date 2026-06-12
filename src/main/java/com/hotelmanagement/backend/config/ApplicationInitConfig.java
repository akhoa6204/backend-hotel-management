package com.hotelmanagement.backend.config;

import com.hotelmanagement.backend.entity.*;
import com.hotelmanagement.backend.enums.DiscountScope;
import com.hotelmanagement.backend.enums.UserRole;
import com.hotelmanagement.backend.enums.DiscountType;
import com.hotelmanagement.backend.enums.RoomStatus;
import com.hotelmanagement.backend.enums.ServiceType;
import com.hotelmanagement.backend.repository.RoleRepository;
import com.hotelmanagement.backend.repository.UserRepository;
import com.hotelmanagement.backend.repository.PermissionRepository;
import com.hotelmanagement.backend.repository.AmenityRepository;
import com.hotelmanagement.backend.repository.RoomRepository;
import com.hotelmanagement.backend.repository.RoomTypeRepository;
import com.hotelmanagement.backend.repository.PromotionRepository;
import com.hotelmanagement.backend.repository.ExtraServiceRepository;
import com.hotelmanagement.backend.repository.ShiftRepository;
import com.hotelmanagement.backend.repository.StaffShiftAssignmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import com.hotelmanagement.backend.enums.StaffPosition;

@Slf4j
@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ApplicationInitConfig {

    PasswordEncoder passwordEncoder;

    @NonFinal
    static final String ADMIN_EMAIL = "admin@diamondsea.hotel.com";

    @NonFinal
    static final String ADMIN_PASSWORD = "admin";

    @Bean
    ApplicationRunner applicationRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AmenityRepository amenityRepository,
            RoomTypeRepository roomTypeRepository,
            RoomRepository roomRepository,
            PromotionRepository promotionRepository,
            ExtraServiceRepository extraServiceRepository,
            ShiftRepository shiftRepository,
            StaffShiftAssignmentRepository staffShiftAssignmentRepository
    ) {

        return args -> {

            if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
                return;
            }

            Permisson bookingCreate = permissionRepository.save(
                    Permisson.builder()
                            .name("BOOKING_CREATE")
                            .description("Create booking")
                            .build()
            );

            Permisson bookingUpdate = permissionRepository.save(
                    Permisson.builder()
                            .name("BOOKING_UPDATE")
                            .description("Update booking")
                            .build()
            );

            Permisson paymentManage = permissionRepository.save(
                    Permisson.builder()
                            .name("PAYMENT_MANAGE")
                            .description("Manage payment")
                            .build()
            );

            Permisson housekeepingManage = permissionRepository.save(
                    Permisson.builder()
                            .name("HOUSEKEEPING_MANAGE")
                            .description("Manage housekeeping")
                            .build()
            );

            HashSet<Permisson> adminPermissions = new HashSet<>();
            adminPermissions.add(bookingCreate);
            adminPermissions.add(bookingUpdate);
            adminPermissions.add(paymentManage);
            adminPermissions.add(housekeepingManage);

            Role userRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.USER.name())
                            .description("User role")
                            .build()
            );

            Role adminRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.ADMIN.name())
                            .description("Admin role")
                            .permissions(adminPermissions)
                            .build()
            );

            User admin = User.builder()
                    .fullName("System Admin")
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode(ADMIN_PASSWORD))
                    .role(adminRole)
                    .active(true)
                    .build();

            userRepository.save(admin);

            HashSet<Amenity> allAmenities = new HashSet<>();

            String[] amenityLabels = {
                    "Wifi",
                    "TV",
                    "Air Conditioner",
                    "Bathtub",
                    "Mini Bar",
                    "Balcony",
                    "Hair Dryer",
                    "Safe Box",
                    "Coffee Machine",
                    "Ocean View",
                    "Work Desk",
                    "Wardrobe",
                    "Kettle",
                    "Smart TV",
                    "Netflix",
                    "Sofa",
                    "Dining Table",
                    "Shower",
                    "Slippers",
                    "Bathrobe"
            };

            for (String label : amenityLabels) {
                Amenity amenity = amenityRepository.save(
                        Amenity.builder()
                                .label(label)
                                .build()
                );

                allAmenities.add(amenity);
            }

            HashSet<Amenity> standardAmenities = new HashSet<>();
            HashSet<Amenity> superiorAmenities = new HashSet<>();
            HashSet<Amenity> deluxeAmenities = new HashSet<>();
            HashSet<Amenity> premiumAmenities = new HashSet<>();
            HashSet<Amenity> executiveAmenities = new HashSet<>();
            HashSet<Amenity> presidentialAmenities = new HashSet<>();

            int index = 0;
            for (Amenity amenity : allAmenities) {
                if (index < 5) standardAmenities.add(amenity);
                if (index < 8) superiorAmenities.add(amenity);
                if (index < 10) deluxeAmenities.add(amenity);
                if (index < 13) premiumAmenities.add(amenity);
                if (index < 16) executiveAmenities.add(amenity);
                presidentialAmenities.add(amenity);
                index++;
            }

            RoomType standardRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("STANDARD")
                            .description("Standard room")
                            .capacity(2)
                            .basePrice(BigDecimal.valueOf(100))
                            .amenities(standardAmenities)
                            .active(true)
                            .build()
            );

            RoomType superiorRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("SUPERIOR")
                            .description("Superior room")
                            .capacity(2)
                            .basePrice(BigDecimal.valueOf(150))
                            .amenities(superiorAmenities)
                            .active(true)
                            .build()
            );

            RoomType deluxeRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("DELUXE")
                            .description("Deluxe room")
                            .capacity(3)
                            .basePrice(BigDecimal.valueOf(220))
                            .amenities(deluxeAmenities)
                            .active(true)
                            .build()
            );

            RoomType premiumRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("PREMIUM")
                            .description("Premium room")
                            .capacity(4)
                            .basePrice(BigDecimal.valueOf(320))
                            .amenities(premiumAmenities)
                            .active(true)
                            .build()
            );

            RoomType executiveRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("EXECUTIVE")
                            .description("Executive room")
                            .capacity(4)
                            .basePrice(BigDecimal.valueOf(450))
                            .amenities(executiveAmenities)
                            .active(true)
                            .build()
            );

            RoomType presidentialRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("PRESIDENTIAL")
                            .description("Presidential suite")
                            .capacity(6)
                            .basePrice(BigDecimal.valueOf(1000))
                            .amenities(presidentialAmenities)
                            .active(true)
                            .build()
            );

            RoomType[] roomTypes = {
                    standardRoomType,
                    superiorRoomType,
                    deluxeRoomType,
                    premiumRoomType,
                    executiveRoomType,
                    presidentialRoomType
            };

            String[][] roomNames = {
                    {"A101", "A102", "A103"},
                    {"B201", "B202", "B203"},
                    {"C301", "C302", "C303"},
                    {"D401", "D402", "D403"},
                    {"E501", "E502", "E503"},
                    {"F601", "F602", "F603"}
            };

            for (int i = 0; i < roomTypes.length; i++) {
                for (String roomName : roomNames[i]) {
                    roomRepository.save(
                            Room.builder()
                                    .name(roomName)
                                    .roomType(roomTypes[i])
                                    .status(RoomStatus.VACANT_CLEAN)
                                    .active(true)
                                    .build()
                    );
                }
            }

            String[][] services = {
                    {"Laundry", "Laundry and ironing service", "SERVICE", "12"},
                    {"Breakfast Buffet", "Daily breakfast buffet", "SERVICE", "15"},
                    {"Spa", "Relaxing spa service", "SERVICE", "50"},
                    {"Gym", "Fitness center access", "SERVICE", "10"},
                    {"Room Cleaning", "Daily room cleaning", "SERVICE", "8"},
                    {"Airport Shuttle", "Airport transport service", "SERVICE", "25"},
                    {"Dinner Buffet", "International dinner buffet", "SERVICE", "35"},
                    {"Motorbike Rental", "Motorbike rental service", "SERVICE", "20"},
                    {"Swimming Pool", "Swimming pool access", "SERVICE", "5"},
                    {"Baby Sitting", "Professional baby sitting", "SERVICE", "30"},

                    {"Late Checkout", "Checkout after standard time", "EXTRA_FEE", "20"},
                    {"Early Checkin", "Checkin before standard time", "EXTRA_FEE", "20"},
                    {"Pet Fee", "Additional pet fee", "EXTRA_FEE", "15"},
                    {"Smoking Fee", "Smoking penalty fee", "EXTRA_FEE", "50"},
                    {"Extra Bed", "Additional bed fee", "EXTRA_FEE", "25"},
                    {"Mini Bar", "Mini bar consumption fee", "EXTRA_FEE", "18"},
                    {"Damage Fee", "Room damage compensation", "EXTRA_FEE", "100"},
                    {"Lost Key", "Lost room key fee", "EXTRA_FEE", "10"},
                    {"Towel Fee", "Missing towel fee", "EXTRA_FEE", "12"},
                    {"Cleaning Penalty", "Excessive dirty room fee", "EXTRA_FEE", "40"}
            };

            for (String[] service : services) {
                extraServiceRepository.save(
                        ExtraService.builder()
                                .name(service[0])
                                .description(service[1])
                                .type(ServiceType.valueOf(service[2]))
                                .basePrice(BigDecimal.valueOf(Long.parseLong(service[3])))
                                .active(true)
                                .build()
                );
            }

            promotionRepository.save(
                    Promotion.builder()
                            .name("Summer Auto Discount")
                            .description("Auto 15% discount for invoice over 200")
                            .discountType(DiscountType.PERCENTAGE)
                            .discountValue(BigDecimal.valueOf(15))
                            .minTotal(BigDecimal.valueOf(200))
                            .maxDiscountAmount(BigDecimal.valueOf(80))
                            .scope(DiscountScope.INVOICE)
                            .priority(1)
                            .quotaTotal(100)
                            .quotaUsed(0)
                            .stackable(true)
                            .autoApplied(true)
                            .active(true)
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusMonths(6))
                            .build()
            );

            promotionRepository.save(
                    Promotion.builder()
                            .name("Weekend Auto Discount")
                            .description("Auto 10% weekend discount")
                            .discountType(DiscountType.PERCENTAGE)
                            .discountValue(BigDecimal.valueOf(10))
                            .minTotal(BigDecimal.valueOf(150))
                            .maxDiscountAmount(BigDecimal.valueOf(50))
                            .scope(DiscountScope.INVOICE)
                            .priority(2)
                            .quotaTotal(100)
                            .quotaUsed(0)
                            .stackable(true)
                            .autoApplied(true)
                            .active(true)
                            .startDate(LocalDate.now())
                            .endDate(LocalDate.now().plusMonths(6))
                            .build()
            );

            for (int i = 1; i <= 8; i++) {
                promotionRepository.save(
                        Promotion.builder()
                                .name("Promo Code " + i)
                                .code("PROMO" + i)
                                .description("Promotion code number " + i)
                                .discountType(i % 2 == 0 ? DiscountType.PERCENTAGE : DiscountType.FIXED_AMOUNT)
                                .discountValue(i % 2 == 0
                                        ? BigDecimal.valueOf(10)
                                        : BigDecimal.valueOf(25))
                                .minTotal(BigDecimal.valueOf(100))
                                .maxDiscountAmount(BigDecimal.valueOf(100))
                                .scope(DiscountScope.INVOICE)
                                .priority(i + 2)
                                .quotaTotal(50)
                                .quotaUsed(0)
                                .stackable(false)
                                .autoApplied(false)
                                .active(true)
                                .startDate(LocalDate.now())
                                .endDate(LocalDate.now().plusMonths(6))
                                .build()
                );
            }

            Role housekeepingRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.HOUSEKEEPING.name())
                            .description("Housekeeping role")
                            .build()
            );

            Role receptionistRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.RECEPTIONIST.name())
                            .description("Receptionist role")
                            .build()
            );

            User housekeepingUser = User.builder()
                    .fullName("Housekeeping Staff")
                    .email("housekeeping@diamondsea.hotel.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(housekeepingRole)
                    .active(true)
                    .build();

            userRepository.save(housekeepingUser);

            User receptionistUser = User.builder()
                    .fullName("Receptionist Staff")
                    .email("reception@diamondsea.hotel.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(receptionistRole)
                    .active(true)
                    .build();

            userRepository.save(receptionistUser);

            User normalUser = User.builder()
                    .fullName("Customer User")
                    .email("user@diamondsea.hotel.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(userRole)
                    .active(true)
                    .build();

            userRepository.save(normalUser);

            Shift morningShift = shiftRepository.save(
                    Shift.builder()
                            .code("MORNING")
                            .name("Ca sáng")
                            .startTime(LocalTime.of(6, 0))
                            .endTime(LocalTime.of(14, 0))
                            .build()
            );

            Shift afternoonShift = shiftRepository.save(
                    Shift.builder()
                            .code("AFTERNOON")
                            .name("Ca chiều")
                            .startTime(LocalTime.of(14, 0))
                            .endTime(LocalTime.of(22, 0))
                            .build()
            );

            Shift nightShift = shiftRepository.save(
                    Shift.builder()
                            .code("NIGHT")
                            .name("Ca tối")
                            .startTime(LocalTime.of(22, 0))
                            .endTime(LocalTime.of(6, 0))
                            .build()
            );

            Shift officeShift = shiftRepository.save(
                    Shift.builder()
                            .code("OFFICE")
                            .name("Ca hành chính")
                            .startTime(LocalTime.of(8, 0))
                            .endTime(LocalTime.of(17, 0))
                            .build()
            );

            staffShiftAssignmentRepository.save(
                    StaffShiftAssignment.builder()
                            .staff(housekeepingUser)
                            .shift(morningShift)
                            .workDate(LocalDate.now())
                            .position(StaffPosition.HOUSEKEEPING)
                            .build()
            );

            staffShiftAssignmentRepository.save(
                    StaffShiftAssignment.builder()
                            .staff(housekeepingUser)
                            .shift(afternoonShift)
                            .workDate(LocalDate.now().plusDays(1))
                            .position(StaffPosition.HOUSEKEEPING)
                            .build()
            );

            staffShiftAssignmentRepository.save(
                    StaffShiftAssignment.builder()
                            .staff(receptionistUser)
                            .shift(officeShift)
                            .workDate(LocalDate.now())
                            .position(StaffPosition.RECEPTION)
                            .build()
            );

            staffShiftAssignmentRepository.save(
                    StaffShiftAssignment.builder()
                            .staff(receptionistUser)
                            .shift(nightShift)
                            .workDate(LocalDate.now().plusDays(1))
                            .position(StaffPosition.RECEPTION)
                            .build()
            );

            log.warn("Application sample seed data initialized successfully");
        };
    }
}
