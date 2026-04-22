package sp26.group.busticket.infrastructure.initializer;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.enumType.StopTypeEnum;
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
    private final RouteStopRepository routeStopRepository;
    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final CoachTypeRepository coachTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (coachRepository.count() > 0) return; // Tránh nạp lại data nếu đã có

        // 1. Initialize Accounts
        initAccounts();
        Account user = accountRepository.findByEmail("user@gmail.com").get();

        // 2. Initialize Locations
        Location saigon = initLocation("Bến xe Miền Đông", "TP. Hồ Chí Minh", "TERMINAL");
        Location dalat = initLocation("Bến xe Đà Lạt", "Lâm Đồng", "TERMINAL");
        Location danang = initLocation("Bến xe Đà Nẵng", "Đà Nẵng", "TERMINAL");
        Location cantho = initLocation("Bến xe Cần Thơ", "Cần Thơ", "TERMINAL");
        Location hue = initLocation("Bến xe Huế", "Thừa Thiên Huế", "TERMINAL");
        Location hanoi = initLocation("Bến xe Mỹ Đình", "Hà Nội", "TERMINAL");

        // 3. Initialize Routes with proper RouteStop entities
        Route sgDl = initRoute(saigon, dalat, 300f, 420);
        initRouteStop(sgDl, initLocation("Ngã ba Dầu Giây", "Đồng Nai", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 60, 60f);
        initRouteStop(sgDl, initLocation("Trạm nghỉ Xuân Lộc", "Đồng Nai", "INTERMEDIATE"), 2, StopTypeEnum.BOTH, 110, 110f);
        initRouteStop(sgDl, initLocation("TP. Bảo Lộc", "Lâm Đồng", "INTERMEDIATE"), 3, StopTypeEnum.BOTH, 180, 180f);
        initRouteStop(sgDl, initLocation("Ngã 3 Liên Khương", "Lâm Đồng", "INTERMEDIATE"), 4, StopTypeEnum.DROPOFF, 260, 260f);

        Route sgDn = initRoute(saigon, danang, 900f, 960);
        initRouteStop(sgDn, initLocation("Trạm nghỉ Phan Thiết", "Bình Thuận", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 180, 200f);
        initRouteStop(sgDn, initLocation("Trạm nghỉ Nha Trang", "Khánh Hòa", "INTERMEDIATE"), 2, StopTypeEnum.BOTH, 430, 450f);
        initRouteStop(sgDn, initLocation("Trạm nghỉ Quy Nhơn", "Bình Định", "INTERMEDIATE"), 3, StopTypeEnum.BOTH, 650, 680f);

        // New Routes
        Route sgCt = initRoute(saigon, cantho, 170f, 180);
        initRouteStop(sgCt, initLocation("Trạm dừng chân Mekong Rest Stop", "Tiền Giang", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 60, 70f);

        Route ctSg = initRoute(cantho, saigon, 170f, 180);
        initRouteStop(ctSg, initLocation("Trạm dừng chân Cái Bè", "Tiền Giang", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 90, 100f);

        Route dnHue = initRoute(danang, hue, 100f, 120);
        initRouteStop(dnHue, initLocation("Hầm Hải Vân", "Đà Nẵng", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 30, 35f);

        Route hueDn = initRoute(hue, danang, 100f, 120);
        initRouteStop(hueDn, initLocation("Lăng Cô", "Thừa Thiên Huế", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 40, 45f);

        Route sgHn = initRoute(saigon, hanoi, 1700f, 1800); // Khoảng 30 tiếng
        initRouteStop(sgHn, initLocation("Trạm dừng chân Dầu Giây", "Đồng Nai", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 60, 60f);
        initRouteStop(sgHn, initLocation("Trạm dừng chân Phan Thiết", "Bình Thuận", "INTERMEDIATE"), 2, StopTypeEnum.BOTH, 240, 250f);
        initRouteStop(sgHn, initLocation("Trạm dừng chân Nha Trang", "Khánh Hòa", "INTERMEDIATE"), 3, StopTypeEnum.BOTH, 600, 620f);
        initRouteStop(sgHn, initLocation("Trạm dừng chân Quy Nhơn", "Bình Định", "INTERMEDIATE"), 4, StopTypeEnum.BOTH, 900, 930f);
        initRouteStop(sgHn, initLocation("Trạm dừng chân Đà Nẵng", "Đà Nẵng", "INTERMEDIATE"), 5, StopTypeEnum.BOTH, 1200, 1250f);
        initRouteStop(sgHn, initLocation("Trạm dừng chân Đồng Hới", "Quảng Bình", "INTERMEDIATE"), 6, StopTypeEnum.BOTH, 1500, 1550f);

        Route hnSg = initRoute(hanoi, saigon, 1700f, 1800); // Khoảng 30 tiếng
        initRouteStop(hnSg, initLocation("Trạm dừng chân Ninh Bình", "Ninh Bình", "INTERMEDIATE"), 1, StopTypeEnum.BOTH, 120, 130f);
        initRouteStop(hnSg, initLocation("Trạm dừng chân Vinh", "Nghệ An", "INTERMEDIATE"), 2, StopTypeEnum.BOTH, 360, 380f);
        initRouteStop(hnSg, initLocation("Trạm dừng chân Huế", "Thừa Thiên Huế", "INTERMEDIATE"), 3, StopTypeEnum.BOTH, 720, 750f);
        initRouteStop(hnSg, initLocation("Trạm dừng chân Đà Nẵng", "Đà Nẵng", "INTERMEDIATE"), 4, StopTypeEnum.BOTH, 840, 870f);
        initRouteStop(hnSg, initLocation("Trạm dừng chân Quy Nhơn", "Bình Định", "INTERMEDIATE"), 5, StopTypeEnum.BOTH, 1140, 1180f);
        initRouteStop(hnSg, initLocation("Trạm dừng chân Phan Thiết", "Bình Thuận", "INTERMEDIATE"), 6, StopTypeEnum.BOTH, 1560, 1600f);

        // 3.5. Initialize Coach Types
        CoachType typeLimousine = initCoachType("Limousine", "Xe khách cao cấp 22 phòng");
        CoachType typeSleeper = initCoachType("Xe Giường Nằm", "Xe giường nằm tiêu chuẩn 40 chỗ");
        CoachType typeSeater = initCoachType("Xe Ghế Ngồi", "Xe ghế ngồi 30 chỗ");

        // 4. Initialize Coaches & Seats
        Coach limousine = initCoach("51B-12345", typeLimousine, 22);
        Coach sleeper = initCoach("51B-67890", typeSleeper, 40);
        Coach seater = initCoach("51C-11223", typeSeater, 30);

        // 5. Initialize Trips
        Account d1 = accountRepository.findByEmail("driver@gmail.com").get();
        Account d2 = accountRepository.findByEmail("driver2@gmail.com").get();
        Account d3 = accountRepository.findByEmail("driver3@gmail.com").get();
        Account d4 = accountRepository.findByEmail("driver4@gmail.com").get();
        Account d5 = accountRepository.findByEmail("driver5@gmail.com").get();
        Account d6 = accountRepository.findByEmail("driver6@gmail.com").get();

        Account a1 = accountRepository.findByEmail("assistant1@gmail.com").get();
        Account a2 = accountRepository.findByEmail("assistant2@gmail.com").get();

        // Chuyến đi này có thể bắt đầu ngay (lùi 1 phút so với hiện tại)
        Trip trip1 = initTrip(sgDl, limousine, LocalDateTime.now().minusMinutes(1), "0912345678", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d1, a1);
        
        // Chuyến đi này sẽ bị khóa nút bắt đầu (còn 30 phút nữa mới tới giờ)
        Trip trip3 = initTrip(sgDn, sleeper, LocalDateTime.now().plusMinutes(30), "0987654321", 500000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d3, a2);

        Trip trip10_today = initTrip(sgDl, seater, LocalDateTime.now().plusHours(5), "0901230000", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d4, a2);
        Trip trip2 = initTrip(sgDl, limousine, LocalDateTime.now().minusHours(5), "0912345678", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.DEPARTED, d2, a1);

        // New Trips
        Trip trip4 = initTrip(sgCt, seater, LocalDateTime.now().plusDays(1).plusHours(9), "0901112233", 150000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d4, a1);
        Trip trip5 = initTrip(ctSg, seater, LocalDateTime.now().plusDays(1).plusHours(14), "0904445566", 150000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d5, a2);
        Trip trip6 = initTrip(dnHue, limousine, LocalDateTime.now().plusHours(6), "0907778899", 100000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d6, a1);
        Trip trip7 = initTrip(hueDn, limousine, LocalDateTime.now().minusDays(1), "0901231231", 100000, sp26.group.busticket.modules.enumType.TripStatusEnum.COMPLETED, d1, a2);
        Trip trip8 = initTrip(sgHn, sleeper, LocalDateTime.now().plusDays(2).plusHours(10), "0904564564", 1000000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d2, a1);
        Trip trip9 = initTrip(hnSg, sleeper, LocalDateTime.now().plusDays(3).plusHours(18), "0907897897", 1000000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d3, a2);
        Trip trip10 = initTrip(sgDl, seater, LocalDateTime.now().plusDays(1).plusHours(3), "0901230000", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d4, a1);
        Trip trip11 = initTrip(sgDn, seater, LocalDateTime.now().plusHours(7), "0904561111", 500000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d5, a2);
        Trip trip12 = initTrip(sgCt, limousine, LocalDateTime.now().plusDays(1).plusHours(11), "0907892222", 150000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d6, a1);
        Trip trip13 = initTrip(ctSg, sleeper, LocalDateTime.now().plusDays(1).plusHours(16), "0901233333", 150000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d1, a2);
        Trip trip14 = initTrip(dnHue, limousine, LocalDateTime.now().plusHours(4), "0904564444", 100000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d2, a1);
        Trip trip15 = initTrip(hueDn, seater, LocalDateTime.now().minusDays(2), "0907895555", 100000, sp26.group.busticket.modules.enumType.TripStatusEnum.COMPLETED, d3, a2);
        Trip trip16 = initTrip(sgHn, seater, LocalDateTime.now().plusDays(2).plusHours(12), "0901236666", 1000000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d4, a1);
        Trip trip17 = initTrip(hnSg, limousine, LocalDateTime.now().plusDays(3).plusHours(20), "0904567777", 1000000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d5, a2);
        //Trip tripTestCancelBlock = initTrip(sgDl, limousine, LocalDateTime.now().plusMinutes(90), "0900111222", 350000, sp26.group.busticket.modules.enumType.TripStatusEnum.SCHEDULED, d1);

        // 6. Initialize Bookings & Tickets for testing
        initBooking(trip1, user, List.of("A01", "A02"), "Nguyễn Văn A", "0901234567",
                trip1.getRoute().getDepartureLocation(), trip1.getRoute().getArrivalLocation(), "TEST-CHECKIN-123");
        initBooking(trip2, user, List.of("A05", "A06"), "Trần Thị B", "0988776655",
                trip2.getRoute().getDepartureLocation(), trip2.getRoute().getArrivalLocation());
        initBooking(trip4, user, List.of("A01"), "Phạm Thị C", "0911223344",
                trip4.getRoute().getDepartureLocation(), trip4.getRoute().getArrivalLocation());
        initBooking(trip5, user, List.of("A03", "A04"), "Lê Văn D", "0911556677",
                trip5.getRoute().getDepartureLocation(), trip5.getRoute().getArrivalLocation());
        initBooking(trip6, user, List.of("B01"), "Hoàng Thị E", "0911889900",
                trip6.getRoute().getDepartureLocation(), trip6.getRoute().getArrivalLocation());
        initBooking(trip8, user, List.of("A10", "A11", "A12"), "Nguyễn Văn F", "0912340000",
                trip8.getRoute().getDepartureLocation(), trip8.getRoute().getArrivalLocation());
        initBooking(trip9, user, List.of("B05", "B06"), "Trần Thị G", "0912341111",
                trip9.getRoute().getDepartureLocation(), trip9.getRoute().getArrivalLocation());
        initBooking(trip10, user, List.of("A03"), "Lê Văn H", "0912342222",
                trip10.getRoute().getDepartureLocation(), trip10.getRoute().getArrivalLocation());
        initBooking(trip11, user, List.of("A07", "A08"), "Phạm Thị I", "0912343333",
                trip11.getRoute().getDepartureLocation(), trip11.getRoute().getArrivalLocation());
        initBooking(trip12, user, List.of("A02"), "Hoàng Văn K", "0912344444",
                trip12.getRoute().getDepartureLocation(), trip12.getRoute().getArrivalLocation());
        initBooking(trip13, user, List.of("A05", "A06"), "Nguyễn Thị L", "0912345555",
                trip13.getRoute().getDepartureLocation(), trip13.getRoute().getArrivalLocation());
        initBooking(trip14, user, List.of("B02"), "Trần Văn M", "0912346666",
                trip14.getRoute().getDepartureLocation(), trip14.getRoute().getArrivalLocation());
        initBooking(trip16, user, List.of("A09", "A10"), "Lê Thị N", "0912347777",
                trip16.getRoute().getDepartureLocation(), trip16.getRoute().getArrivalLocation());
        initBooking(trip17, user, List.of("B03", "B04"), "Phạm Văn P", "0912348888",
                trip17.getRoute().getDepartureLocation(), trip17.getRoute().getArrivalLocation());

      
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
        if (!accountRepository.existsByEmail("driver4@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver4@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Phạm Văn Lực")
                    .phone("0915151515")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build());
        }
        if (!accountRepository.existsByEmail("driver5@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver5@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Hoàng Minh Khang")
                    .phone("0916161616")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build());
        }
        if (!accountRepository.existsByEmail("driver6@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("driver6@gmail.com")
                    .password(passwordEncoder.encode("driver123"))
                    .fullName("Tài xế Nguyễn Thị Mai")
                    .phone("0917171717")
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build());
        }
        if (!accountRepository.existsByEmail("assistant1@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("assistant1@gmail.com")
                    .password(passwordEncoder.encode("assistant123"))
                    .fullName("Phụ xe Lê Văn Em")
                    .phone("0921212121")
                    .status(StatusEnum.ACTIVE)
                    .role("ASSISTANT")
                    .build());
        }
        if (!accountRepository.existsByEmail("assistant2@gmail.com")) {
            accountRepository.save(Account.builder()
                    .email("assistant2@gmail.com")
                    .password(passwordEncoder.encode("assistant123"))
                    .fullName("Phụ xe Trần Văn Út")
                    .phone("0922222222")
                    .status(StatusEnum.ACTIVE)
                    .role("ASSISTANT")
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

    private Location initLocation(String name, String city, String locationType) {
        return locationRepository.findByName(name)
                .orElseGet(() -> locationRepository.save(Location.builder()
                        .name(name)
                        .city(city)
                        .locationType(locationType)
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

    private CoachType initCoachType(String name, String desc) {
        return coachTypeRepository.findByName(name)
                .orElseGet(() -> {
                    CoachType type = new CoachType();
                    type.setName(name);
                    type.setDescription(desc);
                    return coachTypeRepository.save(type);
                });
    }

    private Coach initCoach(String plate, CoachType type, Integer seats) {
        return coachRepository.findByPlateNumber(plate)
                .orElseGet(() -> {
                    Coach coach = coachRepository.save(Coach.builder()
                            .plateNumber(plate)
                            .coachType(type)
                            .totalSeats(seats)
                            .status(sp26.group.busticket.modules.enumType.CoachStatusEnum.AVAILABLE)
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

    private RouteStop initRouteStop(Route route, Location location, int order,
                                    StopTypeEnum stopType, int offsetMinutes, float distanceFromStart) {
        return routeStopRepository.save(RouteStop.builder()
                .route(route)
                .location(location)
                .stopOrder(order)
                .stopType(stopType)
                .offsetMinutes(offsetMinutes)
                .distanceFromStart(distanceFromStart)
                .build());
    }

    private Trip initTrip(Route route, Coach coach, LocalDateTime depTime, String phone,
                          int price, sp26.group.busticket.modules.enumType.TripStatusEnum status, Account driver, Account assistant) {
        return tripRepository.save(Trip.builder()
                .route(route)
                .coach(coach)
                .departureTime(depTime)
                .arrivalTime(depTime.plusMinutes(route.getDuration()))
                .priceBase(BigDecimal.valueOf(price))
                .contactPhoneNumber(phone)
                .tripStatus(status)
                .driver(driver)
                .assistant(assistant)
                .build());
    }

    private void initBooking(Trip trip, Account user, List<String> seatNumbers, String passName, String passPhone,
                             Location pickupLocation, Location dropoffLocation) {
        initBooking(trip, user, seatNumbers, passName, passPhone, pickupLocation, dropoffLocation, null);
    }

    private void initBooking(Trip trip, Account user, List<String> seatNumbers, String passName, String passPhone,
                             Location pickupLocation, Location dropoffLocation, String firstTicketCode) {
        Booking booking = Booking.builder()
                .user(user)
                .trip(trip)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .totalAmount(trip.getPriceBase().multiply(BigDecimal.valueOf(seatNumbers.size())))
                .status(sp26.group.busticket.modules.enumType.BookingStatusEnum.CONFIRMED)
                .bookingCode("INIT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        booking = bookingRepository.save(booking);

        paymentRepository.save(sp26.group.busticket.modules.entity.Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentMethod("CASH")
                .status(sp26.group.busticket.modules.enumType.PaymentStatusEnum.PAID)
                .paidAt(LocalDateTime.now())
                .build());

        for (int i = 0; i < seatNumbers.size(); i++) {
            String sn = seatNumbers.get(i);
            Seat seat = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId()).stream()
                    .filter(s -> s.getSeatNumber().equals(sn))
                    .findFirst().orElseThrow();

            String tCode = (i == 0 && firstTicketCode != null) 
                    ? firstTicketCode 
                    : "INIT-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            ticketRepository.save(Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .passengerName(passName)
                    .passengerPhone(passPhone)
                    .ticketCode(tCode)
                    .build());
        }
    }
}
