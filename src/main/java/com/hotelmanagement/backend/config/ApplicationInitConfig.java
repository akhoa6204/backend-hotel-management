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

            if (userRepository.existsByEmail(ADMIN_EMAIL)) {
                return;
            }

            String[] entities = {
                    "AMENITY",
                    "BOOKING",
                    "EXTRA_SERVICE",
                    "HOUSEKEEPING_TASK",
                    "INVALIDATED_TOKEN",
                    "INVOICE",
                    "INVOICE_ITEM",
                    "INVOICE_PROMOTION",
                    "PAYMENT",
                    "PERMISSION",
                    "PROMOTION",
                    "ROLE",
                    "ROOM",
                    "ROOM_TYPE",
                    "ROOM_TYPE_IMAGE",
                    "SHIFT",
                    "STAFF_SHIFT_ASSIGNMENT",
                    "USER",
                    "REVIEW"
            };

            String[] actions = {
                    "CREATE",
                    "READ",
                    "UPDATE",
                    "DELETE"
            };

            HashSet<Permisson> allPermissions = new HashSet<>();

            for (String entity : entities) {
                for (String action : actions) {
                    Permisson permission = permissionRepository.save(
                            Permisson.builder()
                                    .name(entity + "_" + action)
                                    .description(formatPermissionDescription(action, entity))
                                    .build()
                    );

                    allPermissions.add(permission);
                }
            }

            HashSet<Permisson> adminPermissions = new HashSet<>(allPermissions);

            HashSet<Permisson> receptionistPermissions = new HashSet<>();
            addCrudPermissions(receptionistPermissions, allPermissions, "BOOKING", false);
            addCrudPermissions(receptionistPermissions, allPermissions, "HOUSEKEEPING_TASK", false);
            addCrudPermissions(receptionistPermissions, allPermissions, "INVOICE", false);
            addCrudPermissions(receptionistPermissions, allPermissions, "INVOICE_ITEM", true);
            addCrudPermissions(receptionistPermissions, allPermissions, "INVOICE_PROMOTION", true);
            addCrudPermissions(receptionistPermissions, allPermissions, "PAYMENT", false);
            addReadUpdatePermissions(receptionistPermissions, allPermissions, "ROOM");
            addReadUpdatePermissions(receptionistPermissions, allPermissions, "ROOM_TYPE");
            addPermission(receptionistPermissions, allPermissions, "PROMOTION_READ");
            addPermission(receptionistPermissions, allPermissions, "SHIFT_READ");
            addPermission(receptionistPermissions, allPermissions, "ROOM_TYPE_IMAGE_READ");
            addPermission(receptionistPermissions, allPermissions, "USER_READ");
            addPermission(receptionistPermissions, allPermissions, "EXTRA_SERVICE_READ");
            addPermission(receptionistPermissions, allPermissions, "AMENITY_READ");
            addPermission(receptionistPermissions, allPermissions, "STAFF_SHIFT_ASSIGNMENT_READ");

            HashSet<Permisson> housekeepingPermissions = new HashSet<>();
            addCrudPermissions(housekeepingPermissions, allPermissions, "HOUSEKEEPING_TASK", false);
            addPermission(housekeepingPermissions, allPermissions, "SHIFT_READ");
            addPermission(housekeepingPermissions, allPermissions, "USER_READ");
            addPermission(housekeepingPermissions, allPermissions, "STAFF_SHIFT_ASSIGNMENT_READ");

            HashSet<Permisson> userPermissions = new HashSet<>();
            addPermission(userPermissions, allPermissions, "BOOKING_CREATE");
            addPermission(userPermissions, allPermissions, "BOOKING_READ");
            addPermission(userPermissions, allPermissions, "BOOKING_UPDATE");
            addPermission(userPermissions, allPermissions, "PAYMENT_CREATE");
            addPermission(userPermissions, allPermissions, "PAYMENT_UPDATE");
            addPermission(userPermissions, allPermissions, "ROOM_READ");
            addPermission(userPermissions, allPermissions, "ROOM_TYPE_READ");
            addPermission(userPermissions, allPermissions, "PROMOTION_READ");
            addPermission(userPermissions, allPermissions, "ROOM_TYPE_IMAGE_READ");
            addPermission(userPermissions, allPermissions, "USER_READ");
            addPermission(userPermissions, allPermissions, "AMENITY_READ");
            addPermission(userPermissions, allPermissions, "INVOICE_READ");
            addPermission(userPermissions, allPermissions, "REVIEW_READ");
            addPermission(userPermissions, allPermissions, "REVIEW_CREATE");

            Role userRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.USER.name())
                            .description("User role")
                            .permissions(userPermissions)
                            .build()
            );

            Role adminRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.ADMIN.name())
                            .description("Admin role")
                            .permissions(adminPermissions)
                            .build()
            );

            Role managerRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.MANAGER.name())
                            .description("Manager role")
                            .permissions(new HashSet<>(allPermissions))
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

            for (int i = 1; i <= 2; i++) {
                userRepository.save(
                        User.builder()
                                .fullName("Manager " + i)
                                .email("manager" + i + "@diamondsea.hotel.com")
                                .password(passwordEncoder.encode("123456"))
                                .role(managerRole)
                                .active(true)
                                .build()
                );
            }

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
                            .basePrice(BigDecimal.valueOf(300000))
                            .amenities(standardAmenities)
                            .active(true)
                            .build()
            );

            RoomType superiorRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("SUPERIOR")
                            .description("Superior room")
                            .capacity(2)
                            .basePrice(BigDecimal.valueOf(450000))
                            .amenities(superiorAmenities)
                            .active(true)
                            .build()
            );

            RoomType deluxeRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("DELUXE")
                            .description("Deluxe room")
                            .capacity(3)
                            .basePrice(BigDecimal.valueOf(750000))
                            .amenities(deluxeAmenities)
                            .active(true)
                            .build()
            );

            RoomType premiumRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("PREMIUM")
                            .description("Premium room")
                            .capacity(4)
                            .basePrice(BigDecimal.valueOf(1000000))
                            .amenities(premiumAmenities)
                            .active(true)
                            .build()
            );

            RoomType executiveRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("EXECUTIVE")
                            .description("Executive room")
                            .capacity(4)
                            .basePrice(BigDecimal.valueOf(1250000))
                            .amenities(executiveAmenities)
                            .active(true)
                            .build()
            );

            RoomType presidentialRoomType = roomTypeRepository.save(
                    RoomType.builder()
                            .name("PRESIDENTIAL")
                            .description("Presidential suite")
                            .capacity(6)
                            .basePrice(BigDecimal.valueOf(1500000))
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
                    {"Laundry", "Laundry and ironing service", "SERVICE", "12000"},
                    {"Breakfast Buffet", "Daily breakfast buffet", "SERVICE", "150000"},
                    {"Spa", "Relaxing spa service", "SERVICE", "500000"},
                    {"Gym", "Fitness center access", "SERVICE", "100000"},
                    {"Room Cleaning", "Daily room cleaning", "SERVICE", "80000"},
                    {"Airport Shuttle", "Airport transport service", "SERVICE", "250000"},
                    {"Dinner Buffet", "International dinner buffet", "SERVICE", "350000"},
                    {"Motorbike Rental", "Motorbike rental service", "SERVICE", "200000"},
                    {"Swimming Pool", "Swimming pool access", "SERVICE", "50000"},
                    {"Baby Sitting", "Professional baby sitting", "SERVICE", "300000"},

                    {"Late Checkout", "Checkout after standard time", "EXTRA_FEE", "200000"},
                    {"Early Checkin", "Checkin before standard time", "EXTRA_FEE", "200000"},
                    {"Pet Fee", "Additional pet fee", "EXTRA_FEE", "150000"},
                    {"Smoking Fee", "Smoking penalty fee", "EXTRA_FEE", "500000"},
                    {"Extra Bed", "Additional bed fee", "EXTRA_FEE", "250000"},
                    {"Mini Bar", "Mini bar consumption fee", "EXTRA_FEE", "180000"},
                    {"Damage Fee", "Room damage compensation", "EXTRA_FEE", "1000000"},
                    {"Lost Key", "Lost room key fee", "EXTRA_FEE", "100000"},
                    {"Towel Fee", "Missing towel fee", "EXTRA_FEE", "120000"},
                    {"Cleaning Penalty", "Excessive dirty room fee", "EXTRA_FEE", "400000"}
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
                            .description("Auto 15% discount for invoice over 2,000,000 VND")
                            .discountType(DiscountType.PERCENTAGE)
                            .discountValue(BigDecimal.valueOf(15))
                            .minTotal(BigDecimal.valueOf(2000000))
                            .maxDiscountAmount(BigDecimal.valueOf(800000))
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
                            .description("Auto 10% weekend discount for invoice over 1,500,000 VND")
                            .discountType(DiscountType.PERCENTAGE)
                            .discountValue(BigDecimal.valueOf(10))
                            .minTotal(BigDecimal.valueOf(1500000))
                            .maxDiscountAmount(BigDecimal.valueOf(500000))
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
                                        : BigDecimal.valueOf(250000))
                                .minTotal(BigDecimal.valueOf(1000000))
                                .maxDiscountAmount(BigDecimal.valueOf(1000000))
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
                            .permissions(housekeepingPermissions)
                            .build()
            );

            Role receptionistRole = roleRepository.save(
                    Role.builder()
                            .name(UserRole.RECEPTIONIST.name())
                            .description("Receptionist role")
                            .permissions(receptionistPermissions)
                            .build()
            );

            User[] housekeepingUsers = new User[3];
            for (int i = 0; i < housekeepingUsers.length; i++) {
                housekeepingUsers[i] = userRepository.save(
                        User.builder()
                                .fullName("Housekeeping Staff " + (i + 1))
                                .email(i == 0
                                        ? "housekeeping@diamondsea.hotel.com"
                                        : "housekeeping" + (i + 1) + "@diamondsea.hotel.com")
                                .password(passwordEncoder.encode("123456"))
                                .role(housekeepingRole)
                                .active(true)
                                .build()
                );
            }

            User[] receptionistUsers = new User[3];
            for (int i = 0; i < receptionistUsers.length; i++) {
                receptionistUsers[i] = userRepository.save(
                        User.builder()
                                .fullName("Receptionist Staff " + (i + 1))
                                .email(i == 0
                                        ? "reception@diamondsea.hotel.com"
                                        : "reception" + (i + 1) + "@diamondsea.hotel.com")
                                .password(passwordEncoder.encode("123456"))
                                .role(receptionistRole)
                                .active(true)
                                .build()
                );
            }

            for (int i = 1; i <= 3; i++) {
                userRepository.save(
                        User.builder()
                                .fullName("Customer User " + i)
                                .email(i == 1
                                        ? "user@diamondsea.hotel.com"
                                        : "user" + i + "@diamondsea.hotel.com")
                                .password(passwordEncoder.encode("123456"))
                                .role(userRole)
                                .active(true)
                                .build()
                );
            }

            User housekeepingUser = housekeepingUsers[0];
            User receptionistUser = receptionistUsers[0];

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

    private String formatPermissionDescription(String action, String entity) {
        String formattedAction = action.substring(0, 1).toUpperCase() + action.substring(1).toLowerCase();
        String formattedEntity = entity.toLowerCase().replace("_", " ");
        return formattedAction + " " + formattedEntity;
    }

    private void addCrudPermissions(
            HashSet<Permisson> targetPermissions,
            HashSet<Permisson> allPermissions,
            String entity,
            boolean includeDelete
    ) {
        addPermission(targetPermissions, allPermissions, entity + "_CREATE");
        addPermission(targetPermissions, allPermissions, entity + "_READ");
        addPermission(targetPermissions, allPermissions, entity + "_UPDATE");

        if (includeDelete) {
            addPermission(targetPermissions, allPermissions, entity + "_DELETE");
        }
    }

    private void addReadUpdatePermissions(
            HashSet<Permisson> targetPermissions,
            HashSet<Permisson> allPermissions,
            String entity
    ) {
        addPermission(targetPermissions, allPermissions, entity + "_READ");
        addPermission(targetPermissions, allPermissions, entity + "_UPDATE");
    }

    private void addPermission(
            HashSet<Permisson> targetPermissions,
            HashSet<Permisson> allPermissions,
            String permissionName
    ) {
        Permisson permission = allPermissions.stream()
                .filter(item -> item.getName().equals(permissionName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permissionName));

        targetPermissions.add(permission);
    }
}
