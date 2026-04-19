package sp26.group.busticket.infrastructure.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final RouteRepository routeRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Initialize Accounts
        initAccounts();

        // 2. Initialize Locations
        Location saigon = initLocation("Sài Gòn", "TP. Hồ Chí Minh");
        Location dalat = initLocation("Đà Lạt", "Lâm Đồng");
        Location danang = initLocation("Đà Nẵng", "Đà Nẵng");

        // 3. Initialize Routes
        Route sgDl = initRoute(saigon, dalat, 300f, 420);
        Route sgDn = initRoute(saigon, danang, 900f, 960);

        // 4. Initialize Coaches & Seats
        Coach limousine = initCoach("51B-12345", "Limousine 22 giường", 22);
        Coach sleeper = initCoach("51B-67890", "Xe giường nằm 40 chỗ", 40);

        // 5. Initialize Trips
        initTrip(sgDl, limousine, LocalDateTime.now().plusHours(2), "0912345678", 350000);
        initTrip(sgDl, limousine, LocalDateTime.now().plusDays(1).withHour(22).withMinute(0), "0912345678", 350000);
        initTrip(sgDn, sleeper, LocalDateTime.now().plusHours(5), "0987654321", 500000);
    }

    private void initAccounts() {
        if (!accountRepository.existsByEmail("admin@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("admin@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .fullName("System Administrator")
                    .phone("0123456789")
                    .status(StatusEnum.ACTIVE)
                    .role("ADMIN")
                    .build());
        }
        if (!accountRepository.existsByEmail("user@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("user@gmail.com")
                    .password(passwordEncoder.encode("user123"))
                    .fullName("Nguyễn Văn Khách")
                    .phone("0909999999")
                    .status(StatusEnum.ACTIVE)
                    .role("USER")
                    .build());
        }
        if (!accountRepository.existsByEmail("staff@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("staff@gmail.com")
                    .password(passwordEncoder.encode("staff123"))
                    .fullName("Nhân viên quầy")
                    .phone("0988888888")
                    .status(StatusEnum.ACTIVE)
                    .role("STAFF")
                    .build());
        }
        if (!accountRepository.existsByEmail("guest@busticket.com")) {
            accountRepository.save(Account.builder()
                    .email("guest@busticket.com")
                    .password(passwordEncoder.encode("no-login"))
                    .fullName("Khách vãng lai")
                    .phone("0000000000")
                    .status(StatusEnum.ACTIVE)
                    .role("USER")
                    .build());
        }
    }

    private Location initLocation(String name, String city) {
        return locationRepository.findByName(name)
                .orElseGet(() -> locationRepository.save(Location.builder()
                        .name(name)
                        .city(city)
                        .build()));
    }

    private Route initRoute(Location dep, Location arr, Float dist, Integer dur) {
        // Simple check to avoid duplicates (could be improved)
        return routeRepository.findAll().stream()
                .filter(r -> r.getDepartureLocation().getId().equals(dep.getId()) && 
                             r.getArrivalLocation().getId().equals(arr.getId()))
                .findFirst()
                .orElseGet(() -> routeRepository.save(Route.builder()
                        .departureLocation(dep)
                        .arrivalLocation(arr)
                        .distance(dist)
                        .duration(dur)
                        .build()));
    }

    private Coach initCoach(String plate, String type, Integer seats) {
        return coachRepository.findByPlateNumber(plate)
                .orElseGet(() -> {
                    Coach coach = coachRepository.save(Coach.builder()
                            .plateNumber(plate)
                            .coachType(type)
                            .totalSeats(seats)
                            .build());
                    
                    // Generate Seats
                    List<Seat> seatList = new ArrayList<>();
                    int half = seats / 2;
                    for (int i = 1; i <= half; i++) {
                        seatList.add(Seat.builder().coach(coach).seatNumber("A" + String.format("%02d", i)).floor(1).build());
                        seatList.add(Seat.builder().coach(coach).seatNumber("B" + String.format("%02d", i)).floor(2).build());
                    }
                    seatRepository.saveAll(seatList);
                    return coach;
                });
    }

    private void initTrip(Route route, Coach coach, LocalDateTime depTime, String phone, int price) {
        tripRepository.save(Trip.builder()
                .route(route)
                .coach(coach)
                .departureTime(depTime)
                .arrivalTime(depTime.plusMinutes(route.getDuration()))
                .priceBase(BigDecimal.valueOf(price))
                .contact_phoneNumber(phone)
                .build());
    }
}
