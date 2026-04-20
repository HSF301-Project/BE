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
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (coachRepository.count() > 0) return; // Tránh nạp lại data nếu đã có

        // 1. Initialize Accounts
        initAccounts();
        Account user = accountRepository.findByEmail("user@gmail.com").get();

        // 2. Initialize Locations
        Location saigon = initLocation("Bến xe Miền Đông", "TP. Hồ Chí Minh");
        Location dalat = initLocation("Bến xe Đà Lạt", "Lâm Đồng");
        Location danang = initLocation("Bến xe Đà Nẵng", "Đà Nẵng");

        // 3. Initialize Routes
        Route sgDl = initRoute(saigon, dalat, 300f, 420);
        Route sgDn = initRoute(saigon, danang, 900f, 960);

        // 4. Initialize Coaches & Seats
        Coach limousine = initCoach("51B-12345", "Xe Limousine", 22);
        Coach sleeper = initCoach("51B-67890", "Xe khách", 40);

        // 5. Initialize Trips
        Account d1 = accountRepository.findByEmail("driver@gmail.com").get();
        Account d2 = accountRepository.findByEmail("driver2@gmail.com").get();
        Account d3 = accountRepository.findByEmail("driver3@gmail.com").get();

        Trip trip1 = initTrip(sgDl, limousine, LocalDateTime.now().plusHours(1), "0912345678", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d1);
        Trip trip2 = initTrip(sgDl, limousine, LocalDateTime.now().minusHours(5), "0912345678", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.DEPARTED, d2);
        Trip trip3 = initTrip(sgDn, sleeper, LocalDateTime.now().plusHours(5), "0987654321", 500000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d3);

        // 6. Initialize Bookings & Tickets for testing
        initBooking(trip1, user, List.of("A01", "A02"), "Nguyễn Văn A", "0901234567");
        initBooking(trip2, user, List.of("A05", "A06"), "Trần Thị B", "0988776655");
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
        if (!accountRepository.existsByEmail("driver@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Nguyễn Văn Lái")
                    .phone("0912121212")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build());
        }
        if (!accountRepository.existsByEmail("driver2@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver2@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Trần Văn Đường")
                    .phone("0913131313")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build());
        }
        if (!accountRepository.existsByEmail("driver3@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver3@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Lê Công Thành")
                    .phone("0914141414")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
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

    private Trip initTrip(Route route, Coach coach, LocalDateTime depTime, String phone, int price, sp26.group.busticket.modules.enumType.TripStatusEnum status, Account driver) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .coach(coach)
                .departureTime(depTime)
                .arrivalTime(depTime.plusMinutes(route.getDuration()))
                .priceBase(BigDecimal.valueOf(price))
                .contact_phoneNumber(phone)
                .tripStatus(status)
                .driver(driver)
                .build());
    }

    private void initBooking(Trip trip, Account user, List<String> seatNumbers, String passName, String passPhone) {
        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .totalAmount(trip.getPriceBase().multiply(BigDecimal.valueOf(seatNumbers.size())))
                .status(sp26.group.busticket.modules.enumType.BookingStatusEnum.CONFIRMED)
                .build();
        booking = bookingRepository.save(booking);

        paymentRepository.save(sp26.group.busticket.modules.entity.Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentMethod("CASH")
                .status(sp26.group.busticket.modules.enumType.PaymentStatusEnum.PAID)
                .paidAt(LocalDateTime.now())
                .build());

        for (String sn : seatNumbers) {
            Seat seat = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId()).stream()
                    .filter(s -> s.getSeatNumber().equals(sn))
                    .findFirst().orElseThrow();

            ticketRepository.save(Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .passengerName(passName)
                    .passengerPhone(passPhone)
                    .ticketCode("INIT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .build());
        }
    }
}
