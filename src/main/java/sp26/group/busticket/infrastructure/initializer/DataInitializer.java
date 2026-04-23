package sp26.group.busticket.infrastructure.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.enumType.*;
import sp26.group.busticket.modules.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;
    private final RouteStopRepository routeStopRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoachTypeRepository coachTypeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Initialize Accounts
        initAccounts();

        // 2. Initialize Locations (TERMINALs with specific addresses)
        Location hnMyDinh = initLocation("Bến xe Mỹ Đình", "Hà Nội", "20 Phạm Hùng, Mỹ Đình, Nam Từ Liêm", "TERMINAL");
        Location hnGiapBat = initLocation("Bến xe Giáp Bát", "Hà Nội", "Giải Phóng, Hoàng Mai", "TERMINAL");
        Location hcmMienDong = initLocation("Bến xe Miền Đông", "TP. Hồ Chí Minh", "292 Đinh Bộ Lĩnh, P.26, Bình Thạnh", "TERMINAL");
        Location hcmMienTay = initLocation("Bến xe Miền Tây", "TP. Hồ Chí Minh", "395 Kinh Dương Vương, An Lạc, Bình Tân", "TERMINAL");
        Location dnTrungTam = initLocation("Bến xe Trung tâm Đà Nẵng", "Đà Nẵng", "201 Tôn Đức Thắng, Hòa Minh, Liên Chiểu", "TERMINAL");
        Location dlLienTinh = initLocation("Bến xe Liên tỉnh Đà Lạt", "Lâm Đồng", "01 Tô Hiến Thành, Phường 3", "TERMINAL");
        Location ctTrungTam = initLocation("Bến xe Trung tâm Cần Thơ", "Cần Thơ", "QL1A, Hưng Thạnh, Cái Răng", "TERMINAL");
        Location vtVungTau = initLocation("Bến xe Vũng Tàu", "Bà Rịa - Vũng Tàu", "192 Nam Kỳ Khởi Nghĩa, Phường 3", "TERMINAL");
        Location ntPhiaNam = initLocation("Bến xe Phía Nam Nha Trang", "Khánh Hòa", "Km số 6 đường 23/10, Vĩnh Trung", "TERMINAL");
        Location lcLaoCai = initLocation("Bến xe Lào Cai", "Lào Cai", "Phố Mới, TP. Lào Cai", "TERMINAL");

        // Intermediate STOPS
        Location stopPhuTho = initLocation("Trạm dừng chân Phú Thọ", "Phú Thọ", "Km 98 Cao tốc Nội Bài - Lào Cai", "STOP");
        Location stopXuanLoc = initLocation("Trạm dừng chân Xuân Lộc", "Đồng Nai", "QL1A, Xuân Lộc", "STOP");
        Location stopBaoLoc = initLocation("Trạm dừng chân Bảo Lộc", "Lâm Đồng", "QL20, Bảo Lộc", "STOP");
        Location stopMekong = initLocation("Mekong Rest Stop", "Tiền Giang", "QL1A, Châu Thành", "STOP");
        Location stopHaiVan = initLocation("Trạm dừng chân Hải Vân", "Đà Nẵng", "Nam hầm Hải Vân", "STOP");

        // 3. Initialize Coach Types
        CoachType t1 = initCoachType("Limousine Phòng", "Xe cao cấp, mỗi khách một phòng riêng biệt.");
        CoachType t2 = initCoachType("Xe Giường nằm", "Xe giường nằm tiêu chuẩn.");
        CoachType t3 = initCoachType("Xe Cabin Cung Điện", "Hạng thương gia cao cấp.");
        CoachType t4 = initCoachType("Xe Ghế Ngồi", "Ghế ngả thoải mái.");
        CoachType t5 = initCoachType("Limousine Ghế", "Dòng xe Transit cải tiến.");

        // 4. Initialize Coaches
        Coach c1 = initCoach("29B-123.45", t1, 22);
        Coach c2 = initCoach("51B-678.90", t2, 40);
        Coach c3 = initCoach("43B-111.22", t3, 20);
        Coach c4 = initCoach("49B-555.66", t4, 29);
        Coach c5 = initCoach("72B-999.88", t5, 9);

        // 5. Initialize Routes with calculated durations
        // Formula: duration = (distance / 50) * 1.2 * 60
        Route r1 = initRoute("HN-LC-01", hnMyDinh, lcLaoCai, 320f); // 460 mins
        initRouteStop(r1, stopPhuTho, 1, StopTypeEnum.BOTH, 180, 150f);

        Route r2 = initRoute("HCM-DL-01", hcmMienDong, dlLienTinh, 308f); // 443 mins
        initRouteStop(r2, stopXuanLoc, 1, StopTypeEnum.BOTH, 150, 100f);
        initRouteStop(r2, stopBaoLoc, 2, StopTypeEnum.BOTH, 280, 190f);

        Route r3 = initRoute("HCM-VT-01", hcmMienTay, vtVungTau, 110f); // 158 mins
        Route r4 = initRoute("HCM-CT-01", hcmMienTay, ctTrungTam, 165f); // 237 mins
        initRouteStop(r4, stopMekong, 1, StopTypeEnum.BOTH, 100, 70f);

        Route r5 = initRoute("DN-HUE-01", dnTrungTam, initLocation("Bến xe Phía Nam Huế", "Thừa Thiên Huế", "An Cựu, TP. Huế", "TERMINAL"), 105f); // 151 mins
        initRouteStop(r5, stopHaiVan, 1, StopTypeEnum.BOTH, 40, 30f);

        // 6. Initialize Trips
        Account dr1 = accountRepository.findByEmail("driver1@gmail.com").orElseThrow();
        Account as1 = accountRepository.findByEmail("assistant1@gmail.com").orElseThrow();

        // Trip 1: Soon (45 mins)
        initTrip(r1, c1, LocalDateTime.now().plusMinutes(45), "0912345678", 350000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 2: Later (2 hours)
        initTrip(r2, c2, LocalDateTime.now().plusHours(2), "0987654321", 280000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 3: HCM - Vung Tau (Soon)
        initTrip(r3, c5, LocalDateTime.now().plusMinutes(30), "0901112223", 180000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 4: HCM - Can Tho (Afternoon)
        initTrip(r4, c2, LocalDateTime.now().plusHours(5), "0904445556", 200000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 5: Da Nang - Hue
        initTrip(r5, c3, LocalDateTime.now().plusHours(7), "0907778889", 150000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 6: Hanoi - Hai Phong
        initTrip(r1, c4, LocalDateTime.now().plusHours(1), "0909990001", 120000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 7: Finished Trip
        initTrip(r3, c5, LocalDateTime.now().minusDays(1), "0911223344", 180000, TripStatusEnum.COMPLETED, dr1, as1);
        
        // Trip 8: Running Trip
        initTrip(r4, c2, LocalDateTime.now().minusMinutes(30), "0922334455", 200000, TripStatusEnum.DEPARTED, dr1, as1);
        
        // Trip 9: Night Trip
        initTrip(r2, c1, LocalDateTime.now().plusHours(12), "0933445566", 350000, TripStatusEnum.SCHEDULED, dr1, as1);
        
        // Trip 10: Tomorrow Trip
        initTrip(r1, c3, LocalDateTime.now().plusDays(1), "0944556677", 400000, TripStatusEnum.SCHEDULED, dr1, as1);
    }

    private void initAccounts() {
        createAccount("admin@gmail.com", "admin123", "Quản trị viên Hệ thống", "0123456789", "ADMIN", StatusEnum.ACTIVE);
        createAccount("staff1@gmail.com", "staff123", "Lê Văn Phòng", "0988888881", "STAFF", StatusEnum.ACTIVE);
        createAccount("driver1@gmail.com", "driver123", "Tài xế Nguyễn Văn Lái", "0911000001", "DRIVER", StatusEnum.ACTIVE);
        createAccount("assistant1@gmail.com", "as123", "Phụ xe Lê Văn Em", "0922000001", "ASSISTANT", StatusEnum.ACTIVE);
        createAccount("user1@gmail.com", "user123", "Nguyễn Văn Khách", "0933000001", "USER", StatusEnum.ACTIVE);
    }

    private void createAccount(String email, String pass, String name, String phone, String role, StatusEnum status) {
        if (!accountRepository.existsByEmail(email)) {
            accountRepository.save(Account.builder()
                    .email(email)
                    .password(passwordEncoder.encode(pass))
                    .fullName(name)
                    .phone(phone)
                    .role(role)
                    .status(status)
                    .build());
        }
    }

    private Location initLocation(String name, String city, String address, String type) {
        return locationRepository.findByName(name)
                .orElseGet(() -> locationRepository.save(Location.builder()
                        .name(name)
                        .city(city)
                        .address(address)
                        .locationType(type)
                        .build()));
    }

    private CoachType initCoachType(String name, String desc) {
        return coachTypeRepository.findByName(name)
                .orElseGet(() -> {
                    CoachType ct = new CoachType();
                    ct.setName(name);
                    ct.setDescription(desc);
                    return coachTypeRepository.save(ct);
                });
    }

    private Coach initCoach(String plate, CoachType type, int seats) {
        return coachRepository.findByPlateNumber(plate)
                .orElseGet(() -> {
                    Coach coach = coachRepository.save(Coach.builder()
                            .plateNumber(plate)
                            .coachType(type)
                            .totalSeats(seats)
                            .status(CoachStatusEnum.AVAILABLE)
                            .build());
                    
                    List<Seat> seatList = new ArrayList<>();
                    int floors = (seats > 22) ? 2 : 1;
                    int perFloor = (floors == 2) ? (int) Math.ceil(seats / 2.0) : seats;
                    for (int i = 1; i <= seats; i++) {
                        int f = (i <= perFloor) ? 1 : 2;
                        String prefix = (f == 1) ? "A" : "B";
                        int num = (f == 1) ? i : i - perFloor;
                        seatList.add(Seat.builder().coach(coach).seatNumber(prefix + String.format("%02d", num)).floor(f).build());
                    }
                    seatRepository.saveAll(seatList);
                    return coach;
                });
    }

    private Route initRoute(String code, Location dep, Location arr, float dist) {
        // Calculate duration based on formula: t = (S/50) * 1.2 * 60
        int duration = Math.round((dist / 50.0f) * 1.2f * 60.0f);
        
        return routeRepository.findAll().stream()
                .filter(r -> r.getDepartureLocation().getId().equals(dep.getId()) && 
                             r.getArrivalLocation().getId().equals(arr.getId()))
                .findFirst()
                .orElseGet(() -> routeRepository.save(Route.builder()
                        .routeCode(code)
                        .departureLocation(dep)
                        .arrivalLocation(arr)
                        .distance(dist)
                        .duration(duration)
                        .build()));
    }

    private void initRouteStop(Route route, Location location, int order, StopTypeEnum stopType, int offsetMinutes, float distanceFromStart) {
        routeStopRepository.save(RouteStop.builder()
                .route(route)
                .location(location)
                .stopOrder(order)
                .stopType(stopType)
                .offsetMinutes(offsetMinutes)
                .distanceFromStart(distanceFromStart)
                .build());
    }

    private void initTrip(Route r, Coach c, LocalDateTime dep, String phone, int price, TripStatusEnum s, Account dr, Account as) {
        tripRepository.save(Trip.builder()
                .route(r)
                .coach(c)
                .departureTime(dep)
                .arrivalTime(dep.plusMinutes(r.getDuration()))
                .priceBase(BigDecimal.valueOf(price))
                .contactPhoneNumber(phone)
                .tripStatus(s)
                .driver(dr)
                .assistant(as)
                .build());
    }
}
