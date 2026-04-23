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
    private final PasswordEncoder passwordEncoder;
    private final CoachTypeRepository coachTypeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Initialize Accounts
        initAccounts();

        // 2. Initialize Locations (4 Terminals + 4 Stops)
        Location hcm = initLocation("Bến xe Miền Đông", "TP. Hồ Chí Minh", "292 Đinh Bộ Lĩnh", "TERMINAL");
        Location dl = initLocation("Bến xe Đà Lạt", "Lâm Đồng", "01 Tô Hiến Thành", "TERMINAL");
        Location hn = initLocation("Bến xe Mỹ Đình", "Hà Nội", "20 Phạm Hùng", "TERMINAL");
        Location dn = initLocation("Bến xe Đà Nẵng", "Đà Nẵng", "201 Tôn Đức Thắng", "TERMINAL");

        Location stop1 = initLocation("Trạm dừng chân Xuân Lộc", "Đồng Nai", "QL1A", "STOP");
        Location stop2 = initLocation("Trạm dừng chân Bảo Lộc", "Lâm Đồng", "QL20", "STOP");
        Location stop3 = initLocation("Trạm dừng chân Phan Thiết", "Bình Thuận", "QL1A", "STOP");
        Location stop4 = initLocation("Trạm dừng chân Nha Trang", "Khánh Hòa", "QL1A", "STOP");

        // 3. Initialize 3 Coach Types
        CoachType sleeper = initCoachType("Xe Giường nằm", "Xe giường nằm tiêu chuẩn 40 chỗ.");
        CoachType seat = initCoachType("Xe Ghế Ngồi", "Xe ghế ngồi 29 chỗ.");
        CoachType limousine = initCoachType("Limousine", "Xe Limousine cao cấp 22 phòng.");

        // 4. Initialize 10 Coaches
        for (int i = 1; i <= 4; i++) initCoach("51B-000.0" + i, sleeper, 40);
        for (int i = 5; i <= 7; i++) initCoach("51B-000.0" + i, seat, 29);
        for (int i = 8; i <= 10; i++) {
            String plate = (i == 10) ? "51B-000.10" : "51B-000.0" + i;
            initCoach(plate, limousine, 22);
        }

        // 5. Initialize 1 Route and 1 Return Route
        Route forward = initRoute("HCM-DL-01", hcm, dl, 300f);
        if (routeStopRepository.findByRouteIdOrderByStopOrderAsc(forward.getId()).isEmpty()) {
            initRouteStop(forward, stop1, 1, StopTypeEnum.BOTH, 120, 100f);
            initRouteStop(forward, stop2, 2, StopTypeEnum.BOTH, 240, 200f);
        }

        Route backward = initRoute("DL-HCM-01", dl, hcm, 300f);
        if (routeStopRepository.findByRouteIdOrderByStopOrderAsc(backward.getId()).isEmpty()) {
            initRouteStop(backward, stop2, 1, StopTypeEnum.BOTH, 60, 100f);
            initRouteStop(backward, stop1, 2, StopTypeEnum.BOTH, 180, 200f);
        }

        // 6. Initialize some sample Trips
        Account driver1 = accountRepository.findByEmail("driver1@gmail.com").orElse(null);
        Account as1 = accountRepository.findByEmail("assistant1@gmail.com").orElse(null);
        Coach coach1 = coachRepository.findByPlateNumber("51B-000.01").orElse(null);

        if (driver1 != null && coach1 != null) {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(8).withMinute(0).withSecond(0).withNano(0);
            initTrip(forward, coach1, driver1, as1, tomorrow, new BigDecimal("250000"), "0901234567");
        }
    }

    private void initAccounts() {
        // Admin & Staff
        createAccount("admin@gmail.com", "admin123", "Admin", "0000000000", "ADMIN", StatusEnum.ACTIVE);
        createAccount("staff@gmail.com", "staff123", "Staff", "0000000001", "STAFF", StatusEnum.ACTIVE);
        
        // 10 Drivers
        for (int i = 1; i <= 10; i++) {
            createAccount("driver" + i + "@gmail.com", "driver123", "Driver " + i, "09120000" + String.format("%02d", i), "DRIVER", StatusEnum.ACTIVE);
        }
        
        // 10 Assistants
        for (int i = 1; i <= 10; i++) {
            createAccount("assistant" + i + "@gmail.com", "as123", "Assistant " + i, "09220000" + String.format("%02d", i), "ASSISTANT", StatusEnum.ACTIVE);
        }
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
        int duration = Math.round((dist / 50.0f) * 1.2f * 60.0f);
        return routeRepository.findByDepartureLocationAndArrivalLocation(dep, arr)
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

    private void initTrip(Route route, Coach coach, Account driver, Account assistant, LocalDateTime departure, BigDecimal price, String phone) {
        // Simple existence check: same route, coach, and departure time
        boolean exists = tripRepository.findAll().stream()
                .anyMatch(t -> t.getRoute().getId().equals(route.getId()) 
                        && t.getCoach().getId().equals(coach.getId()) 
                        && t.getDepartureTime().equals(departure));
        
        if (!exists) {
            tripRepository.save(Trip.builder()
                    .route(route)
                    .coach(coach)
                    .driver(driver)
                    .assistant(assistant)
                    .departureTime(departure)
                    .arrivalTime(departure.plusMinutes(route.getDuration()))
                    .priceBase(price)
                    .contactPhoneNumber(phone)
                    .tripStatus(TripStatusEnum.SCHEDULED)
                    .build());
        }
    }
}
