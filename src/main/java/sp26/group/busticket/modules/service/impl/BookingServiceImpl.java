package sp26.group.busticket.modules.service.impl;

import sp26.group.busticket.modules.dto.booking.response.*;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.service.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.account.response.UserProfileDTO;
import sp26.group.busticket.modules.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.modules.dto.booking.request.PassengerInfoDTO;
import sp26.group.busticket.modules.dto.booking.request.StaffBookingRequestDTO;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;
import sp26.group.busticket.modules.enumType.PaymentStatusEnum;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.repository.*;
import sp26.group.busticket.modules.service.BookingService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final LocationRepository locationRepository;
    private final TripService tripService;
    private final PasswordEncoder passwordEncoder;

    private static final int CLEANUP_MINUTES = 7;

    @Override
    @Transactional
    public UUID createBooking(UUID tripId, BookingFormDTO form, Account currentAccount) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        // Chặn đặt vé nếu gần đến thời gian khởi hành dưới 1 tiếng
        if (trip.getDepartureTime().minusHours(1).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Chuyến xe sắp khởi hành trong dưới 1 tiếng, không thể đặt vé trực tuyến. Vui lòng liên đặt vé tại quầy.");
        }

        // Kiểm tra trùng ghế (Concurrency Check)
        List<String> selectedSeatNumbers = form.getPassengers().stream()
                .map(PassengerInfoDTO::getSeatId)
                .toList();

        List<Ticket> existingTickets = ticketRepository.findByBooking_Trip_Id(tripId);
        for (Ticket t : existingTickets) {
            BookingStatusEnum status = t.getBooking().getStatus();
            if ((status == BookingStatusEnum.PENDING || status == BookingStatusEnum.CONFIRMED)
                    && selectedSeatNumbers.contains(t.getSeat().getSeatNumber())) {
                throw new AppException(ErrorCode.INVALID_INPUT,
                        "Ghế " + t.getSeat().getSeatNumber() + " đã bị người khác giữ hoặc đặt thành công!");
            }
        }

        BigDecimal totalAmount = trip.getPriceBase().multiply(BigDecimal.valueOf(form.getPassengers().size()));

        Location pickupLocation = locationRepository.findById(form.getPickupLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm đón không hợp lệ."));
        Location dropoffLocation = locationRepository.findById(form.getDropoffLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm trả không hợp lệ."));

        if (pickupLocation.getId().equals(dropoffLocation.getId())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Điểm đón và điểm trả không được trùng nhau.");
        }

        // Validate pickup time is before dropoff time
        List<TripStopEtaDTO> tripStopEtas = tripService.getTripStopEtas(tripId);
        Integer pickupOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(pickupLocation.getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy thời gian đón hợp lệ."));

        Integer dropoffOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(dropoffLocation.getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy thời gian trả hợp lệ."));

        if (pickupOffset >= dropoffOffset) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Điểm đón phải xảy ra trước điểm trả.");
        }

        Booking booking = Booking.builder()
                .user(currentAccount)
                .trip(trip)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .totalAmount(totalAmount)
                .status(BookingStatusEnum.PENDING)
                .bookingCode("PTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        booking = bookingRepository.save(booking);

        // Tạo Payment ban đầu
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(totalAmount)
                .paymentMethod("VNPAY") // Mặc định hoặc từ form
                .status(PaymentStatusEnum.PENDING)
                .build();
        paymentRepository.save(payment);

        for (PassengerInfoDTO p : form.getPassengers()) {
            Seat seat = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId()).stream()
                    .filter(s -> s.getSeatNumber().equals(p.getSeatId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .passengerName(p.getFullName())
                    .passengerPhone(p.getPhoneNumber())
                    .passengerEmail(p.getEmail())
                    .ticketCode("PTA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .build();
            ticketRepository.save(ticket);
        }

        return booking.getId();
    }

    @Override
    @Transactional
    public UUID createStaffBooking(UUID tripId, StaffBookingRequestDTO form, Account staffAccount) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        // Nhân viên vẫn có thể đặt vé cho đến sát giờ khởi hành 15 phút
        if (trip.getDepartureTime().minusMinutes(15).isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Đã hết thời gian đặt vé cho chuyến xe này (15 phút nữa sẽ đến giờ khởi hành).");
        }

        // 1. Xử lý Account (Ưu tiên tìm Account thật theo SĐT, nếu không có mới dùng GUEST)
        Account userAccount = accountRepository.findByPhone(form.getCustomerPhone())
                .orElseGet(() -> accountRepository.findByEmail("guest@busticket.com")
                        .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy Guest Account mặc định!")));

        // 2. Kiểm tra trùng ghế
        List<String> selectedSeatNumbers = form.getSelectedSeats();
        List<Ticket> existingTickets = ticketRepository.findByBooking_Trip_Id(tripId);
        for (Ticket t : existingTickets) {
            BookingStatusEnum status = t.getBooking().getStatus();
            if ((status == BookingStatusEnum.PENDING || status == BookingStatusEnum.CONFIRMED)
                    && selectedSeatNumbers.contains(t.getSeat().getSeatNumber())) {
                throw new AppException(ErrorCode.INVALID_INPUT,
                        "Ghế " + t.getSeat().getSeatNumber() + " đã bị người khác đặt!");
            }
        }

        // 3. Tạo Booking
        BigDecimal totalAmount = trip.getPriceBase().multiply(BigDecimal.valueOf(form.getSelectedSeats().size()));

        Location pickupLocation = locationRepository.findById(form.getPickupLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm đón không hợp lệ."));
        Location dropoffLocation = locationRepository.findById(form.getDropoffLocationId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Điểm trả không hợp lệ."));

        Booking booking = Booking.builder()
                .user(userAccount) // Dùng account tìm được
                .createdBy(staffAccount)
                .trip(trip)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .totalAmount(totalAmount)
                .status(BookingStatusEnum.CONFIRMED)
                .bookingCode("OFFLINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();
        booking = bookingRepository.save(booking);

        // 4. Tạo Payment (Đã thanh toán)
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(totalAmount)
                .paymentMethod(form.getPaymentMethod() != null ? form.getPaymentMethod() : "CASH")
                .status(PaymentStatusEnum.PAID)
                .paidAt(LocalDateTime.now())
                .build();
        paymentRepository.save(payment);

        // 5. Tạo Tickets (Lưu Tên và SĐT của khách vào từng vé)
        for (String seatNumber : selectedSeatNumbers) {
            Seat seat = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId()).stream()
                    .filter(s -> s.getSeatNumber().equals(seatNumber))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));

            Ticket ticket = Ticket.builder()
                    .booking(booking)
                    .seat(seat)
                    .passengerName(form.getCustomerName())
                    .passengerPhone(form.getCustomerPhone())
                    .passengerEmail(form.getCustomerEmail())
                    .ticketCode("OFFLINE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .build();
            ticketRepository.save(ticket);
        }

        return booking.getId();
    }

    @Override
    @Transactional
    public void linkGuestBookingsToAccount(Account account) {
        // Tìm tất cả vé có SĐT trùng với SĐT của Account mới
        List<Ticket> guestTickets = ticketRepository.findByPassengerPhone(account.getPhone());
        
        // Lấy Guest Account mặc định để kiểm tra
        Account guestAccount = accountRepository.findByEmail("guest@busticket.com").orElse(null);
        if (guestAccount == null) return;

        // Tập hợp các Booking thuộc về GUEST và có vé chứa SĐT này
        List<Booking> bookingsToUpdate = guestTickets.stream()
                .map(Ticket::getBooking)
                .filter(b -> b.getUser().getId().equals(guestAccount.getId()))
                .distinct()
                .toList();

        if (!bookingsToUpdate.isEmpty()) {
            log.info("Linking {} guest bookings to new account: {}", bookingsToUpdate.size(), account.getEmail());
            bookingsToUpdate.forEach(b -> b.setUser(account));
            bookingRepository.saveAll(bookingsToUpdate);
        }
    }

    @Override
    public PaymentResponseDTO getPaymentInfo(UUID bookingId, UUID accountId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem thông tin thanh toán này.");
        }

        Trip trip = booking.getTrip();
        LocalDateTime expiryAt = booking.getCreatedAt().plusMinutes(CLEANUP_MINUTES);
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Calculate pickup and dropoff times
        List<TripStopEtaDTO> tripStopEtas = tripService.getTripStopEtas(trip.getId());
        Integer pickupOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(booking.getPickupLocation().getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm đón đã lưu."));

        Integer dropoffOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(booking.getDropoffLocation().getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm trả đã lưu."));

        LocalDateTime actualPickupTime = trip.getDepartureTime().plusMinutes(pickupOffset);
        LocalDateTime actualDropoffTime = trip.getDepartureTime().plusMinutes(dropoffOffset);

        return PaymentResponseDTO.builder()
                .bookingId(bookingId)
                .expiryTime(expiryAt.format(timeFormatter))
                .expiryTimestamp(expiryAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .fromCity(trip.getRoute().getDepartureLocation().getCity())
                .toCity(trip.getRoute().getArrivalLocation().getCity())
                .pickupLocationName(booking.getPickupLocation().getName())
                .dropoffLocationName(booking.getDropoffLocation().getName())
                .pickupTime(actualPickupTime.format(timeFormatter))
                .dropoffTime(actualDropoffTime.format(timeFormatter))
                .departureTime(trip.getDepartureTime().format(timeFormatter))
                .arrivalTime(trip.getArrivalTime().format(timeFormatter))
                .dateLabel(trip.getDepartureTime().format(dateFormatter))
                .busTypeLabel(trip.getCoach().getCoachType())
                .ticketCount((int) ticketRepository.findAll().stream().filter(t -> t.getBooking().getId().equals(bookingId)).count())
                .totalFormatted(vnFormat.format(booking.getTotalAmount()))
                .build();
    }

    @Override
    @Transactional
    public void processPayment(UUID bookingId, String paymentMethod, UUID accountId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền thanh toán cho đơn hàng này.");
        }

        // Kiểm tra nếu booking đã bị hủy (ví dụ: quá hạn 7 phút)
        if (booking.getStatus() == BookingStatusEnum.CANCELLED) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Đơn hàng này đã bị hủy do quá thời gian thanh toán. Vui lòng đặt lại ghế!");
        }

        booking.setStatus(BookingStatusEnum.CONFIRMED);
        bookingRepository.save(booking);

        Payment payment = paymentRepository.findByBooking_Id(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy thông tin thanh toán!"));
        
        // Kiểm tra nếu payment đã bị hủy
        if (payment.getStatus() == PaymentStatusEnum.CANCELLED) {
             throw new AppException(ErrorCode.INVALID_INPUT, 
                "Giao dịch thanh toán đã bị hủy. Vui lòng thực hiện đặt vé mới!");
        }

        payment.setStatus(PaymentStatusEnum.PAID);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Override
    public TicketConfirmationDTO getBookingSuccessInfo(UUID bookingId, UUID accountId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem thông tin vé này.");
        }
        
        List<Ticket> tickets = ticketRepository.findAll().stream()
                .filter(t -> t.getBooking().getId().equals(bookingId))
                .collect(Collectors.toList());
        
        if (tickets.isEmpty()) {
            throw new AppException(ErrorCode.BOOKING_NOT_FOUND, "Không tìm thấy thông tin vé");
        }

        Ticket firstTicket = tickets.get(0);
        Trip trip = booking.getTrip();
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", localeVN);
        DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Gộp tất cả số ghế
        String allSeats = tickets.stream()
                .map(t -> t.getSeat().getSeatNumber())
                .collect(Collectors.joining(", "));

        // Lấy thời gian đặt (thanh toán thành công)
        String bookingDate = paymentRepository.findByBooking_Id(bookingId)
                .map(p -> p.getPaidAt() != null ? p.getPaidAt().format(fullDateTimeFormatter) : booking.getCreatedAt().format(fullDateTimeFormatter))
                .orElse(booking.getCreatedAt().format(fullDateTimeFormatter));

        // Calculate pickup and dropoff times
        List<TripStopEtaDTO> tripStopEtas = tripService.getTripStopEtas(trip.getId());
        Integer pickupOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(booking.getPickupLocation().getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm đón đã lưu."));

        Integer dropoffOffset = tripStopEtas.stream()
                .filter(stop -> stop.getStopId().equals(booking.getDropoffLocation().getId()))
                .map(TripStopEtaDTO::getOffsetMinutes)
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm trả đã lưu."));

        LocalDateTime actualPickupTime = trip.getDepartureTime().plusMinutes(pickupOffset);
        LocalDateTime actualDropoffTime = trip.getDepartureTime().plusMinutes(dropoffOffset);

        return TicketConfirmationDTO.builder()
                .id(firstTicket.getId())
                .statusLabel("Đã xác nhận")
                .bookingCode(firstTicket.getTicketCode())
                .fromCityShort(trip.getRoute().getDepartureLocation().getCity().toUpperCase())
                .toCityShort(trip.getRoute().getArrivalLocation().getCity().toUpperCase())
                .departureStation(trip.getRoute().getDepartureLocation().getName())
                .arrivalStation(trip.getRoute().getArrivalLocation().getName())
                .pickupLocationName(booking.getPickupLocation().getName())
                .dropoffLocationName(booking.getDropoffLocation().getName())
                .pickupTime(actualPickupTime.format(timeFormatter))
                .dropoffTime(actualDropoffTime.format(timeFormatter))
                .departureTime(trip.getDepartureTime().format(timeFormatter))
                .arrivalTime(trip.getArrivalTime().format(timeFormatter))
                .departureDateLabel(trip.getDepartureTime().format(dateFormatter))
                .seatLabel(allSeats)
                .licensePlate(trip.getCoach().getPlateNumber())
                .serviceType(trip.getCoach().getCoachType())
                .passengerName(firstTicket.getPassengerName())
                .totalFormatted(vnFormat.format(booking.getTotalAmount()))
                .bookingDate(bookingDate)
                .build();
    }

    @Override
    public List<MyTripResponseDTO> getMyTrips(UUID accountId, String tab) {
        List<Booking> bookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(accountId);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter fullDateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .map(b -> {
                    Trip trip = b.getTrip();
                    BookingStatusEnum status = b.getStatus();
                    
                    // Nếu đã thanh toán nhưng chuyến đi đã kết thúc thì coi là COMPLETED
                    if (status == BookingStatusEnum.CONFIRMED && trip.getArrivalTime().isBefore(now)) {
                        status = BookingStatusEnum.COMPLETED;
                    }

                    // Lấy thời gian thanh toán từ Payment, nếu không có thì lấy thời gian tạo Booking
                    String bookingDate = paymentRepository.findByBooking_Id(b.getId())
                            .map(p -> p.getPaidAt() != null ? p.getPaidAt().format(fullDateTimeFormatter) : b.getCreatedAt().format(fullDateTimeFormatter))
                            .orElse(b.getCreatedAt().format(fullDateTimeFormatter));

                    // Calculate pickup and dropoff times for MyTripResponseDTO
                    List<TripStopEtaDTO> tripStopEtas = tripService.getTripStopEtas(trip.getId());

                    Integer pickupOffset = tripStopEtas.stream()
                            .filter(stop -> stop.getStopId().equals(b.getPickupLocation().getId()))
                            .map(TripStopEtaDTO::getOffsetMinutes)
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm đón đã lưu."));

                    Integer dropoffOffset = tripStopEtas.stream()
                            .filter(stop -> stop.getStopId().equals(b.getDropoffLocation().getId()))
                            .map(TripStopEtaDTO::getOffsetMinutes)
                            .findFirst()
                            .orElseThrow(() -> new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy offset cho điểm trả đã lưu."));

                    LocalDateTime actualPickupTime = trip.getDepartureTime().plusMinutes(pickupOffset);
                    LocalDateTime actualDropoffTime = trip.getDepartureTime().plusMinutes(dropoffOffset);

                    return MyTripResponseDTO.builder()
                            .id(b.getId())
                            .bookingCode(b.getBookingCode())
                            .status(status)
                            .busTypeLabel(trip.getCoach().getCoachType())
                            .daysUntilDeparture(java.time.Duration.between(now, trip.getDepartureTime()).toDays())
                            .fromCity(trip.getRoute().getDepartureLocation().getCity())
                            .departureStation(b.getPickupLocation().getName())
                            .departureTime(trip.getDepartureTime().format(timeFormatter))
                            .toCity(trip.getRoute().getArrivalLocation().getCity())
                            .arrivalStation(b.getDropoffLocation().getName())
                            .arrivalTime(trip.getArrivalTime().format(timeFormatter))
                            .pickupLocationName(b.getPickupLocation().getName())
                            .dropoffLocationName(b.getDropoffLocation().getName())
                            .pickupTime(actualPickupTime.format(timeFormatter))
                            .dropoffTime(actualDropoffTime.format(timeFormatter))
                            .bookingDate(bookingDate)
                            .isCancellable(status == BookingStatusEnum.CONFIRMED && trip.getDepartureTime().minusHours(2).isAfter(now))
                            .build();
                })
                .filter(dto -> {
                    if (tab == null || tab.equalsIgnoreCase("all")) return true;
                    if (tab.equalsIgnoreCase("upcoming")) {
                        return dto.getStatus() == BookingStatusEnum.CONFIRMED || 
                               dto.getStatus() == BookingStatusEnum.PENDING;
                    }
                    return dto.getStatus().name().equalsIgnoreCase(tab);
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileDTO getUserProfile(Account account) {
        long totalTrips = bookingRepository.countByUser_Id(account.getId());

        return UserProfileDTO.builder()
                .fullName(account.getFullName())
                .email(account.getEmail())
                .avatarUrl(null)
                .membershipTier("Vàng")
                .totalTrips((int) totalTrips)
                .membershipLabel("Premium Voyager")
                .build();
    }

    @Override
    @Transactional
    public UUID cancelBooking(UUID bookingId, UUID accountId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền hủy đặt vé này.");
        }

        if (booking.getStatus() == BookingStatusEnum.CANCELLED || booking.getStatus() == BookingStatusEnum.COMPLETED) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không thể hủy đặt vé đã bị hủy hoặc đã hoàn thành.");
        }

        // Chỉ áp dụng quy tắc 2 tiếng cho vé đã thanh toán (CONFIRMED)
        // Với vé đang chờ thanh toán (PENDING), cho phép hủy ngay lập tức
        if (booking.getStatus() == BookingStatusEnum.CONFIRMED) {
            // Kiểm tra nếu chuyến đi đã khởi hành
            if (booking.getTrip().getDepartureTime().isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Không thể hủy đặt vé cho chuyến đi đã khởi hành.");
            }

            // Kiểm tra thời gian hủy: phải trước giờ khởi hành ít nhất 2 tiếng
            if (booking.getTrip().getDepartureTime().minusHours(2).isBefore(LocalDateTime.now())) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể hủy đặt vé trước giờ khởi hành ít nhất 2 tiếng.");
            }
        }

        booking.setStatus(BookingStatusEnum.CANCELLED);
        bookingRepository.save(booking);

        paymentRepository.findByBooking_Id(bookingId).ifPresent(payment -> {
            payment.setStatus(PaymentStatusEnum.CANCELLED);
            paymentRepository.save(payment);
        });

        return booking.getTrip().getId();
    }

    @Override
    public BookingFormDTO getBookingFormFromBooking(UUID bookingId, UUID accountId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem thông tin booking này.");
        }

        List<Ticket> tickets = ticketRepository.findByBooking_Trip_Id(booking.getTrip().getId())
                .stream()
                .filter(t -> t.getBooking().getId().equals(bookingId))
                .toList();

        List<PassengerInfoDTO> passengers = tickets.stream()
                .map(t -> PassengerInfoDTO.builder()
                        .seatId(t.getSeat().getSeatNumber())
                        .seatLabel(t.getSeat().getSeatNumber())
                        .deck(t.getSeat().getFloor() == 1 ? "lower" : "upper")
                        .fullName(t.getPassengerName())
                        .phoneNumber(t.getPassengerPhone())
                        .email(t.getPassengerEmail())
                        .build())
                .collect(Collectors.toList());

        return BookingFormDTO.builder()
                .passengers(passengers)
                .pickupLocationId(booking.getPickupLocation() != null ? booking.getPickupLocation().getId() : null)
                .dropoffLocationId(booking.getDropoffLocation() != null ? booking.getDropoffLocation().getId() : null)
                .build();
    }

    @Override
    public TicketDetailResponseDTO getTicketDetailByBookingCode(String bookingCode, UUID accountId) {
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUser().getId().equals(accountId)) {
            throw new AppException(ErrorCode.FORBIDDEN, "Bạn không có quyền xem vé này.");
        }

        List<Ticket> tickets = ticketRepository.findByBooking_Trip_Id(booking.getTrip().getId())
                .stream()
                .filter(t -> t.getBooking().getId().equals(booking.getId()))
                .toList();

        if (tickets.isEmpty()) {
            throw new AppException(ErrorCode.UNEXPECTED_ERROR, "Không tìm thấy thông tin vé!");
        }

        Ticket firstTicket = tickets.get(0);
        Trip trip = booking.getTrip();
        
        return TicketDetailResponseDTO.builder()
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .bookingTime(booking.getCreatedAt())
                .passengerName(firstTicket.getPassengerName())
                .passengerPhone(firstTicket.getPassengerPhone())
                .passengerEmail(firstTicket.getPassengerEmail())
                .seatNumbers(tickets.stream().map(t -> t.getSeat().getSeatNumber()).toList())
                .routeName(trip.getRoute().getDepartureLocation().getName() + " -> " + trip.getRoute().getArrivalLocation().getName())
                .pickupPointName(booking.getPickupLocation() != null ? booking.getPickupLocation().getName() : "Tại văn phòng")
                .pickupTime(trip.getDepartureTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                .dropoffPointName(booking.getDropoffLocation() != null ? booking.getDropoffLocation().getName() : "Tại văn phòng")
                .dropoffTime(trip.getArrivalTime().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")))
                .coachPlate(trip.getCoach().getPlateNumber())
                .coachType(trip.getCoach().getCoachType())
                .basePrice(trip.getPriceBase().doubleValue())
                .seatCount(tickets.size())
                .totalAmount(booking.getTotalAmount().doubleValue())
                .totalAmountFormatted(String.format("%,.0fđ", booking.getTotalAmount()))
                .build();
    }

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void cleanupExpiredBookings() {
        LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(CLEANUP_MINUTES);
        List<Booking> expiredBookings = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatusEnum.PENDING && b.getCreatedAt().isBefore(expiryThreshold))
                .toList();

        if (!expiredBookings.isEmpty()) {
            log.info("Cleaning up {} expired PENDING bookings", expiredBookings.size());
            expiredBookings.forEach(b -> {
                b.setStatus(BookingStatusEnum.CANCELLED);
                paymentRepository.findByBooking_Id(b.getId()).ifPresent(p -> {
                    p.setStatus(PaymentStatusEnum.CANCELLED);
                    paymentRepository.save(p);
                });
            });
            bookingRepository.saveAll(expiredBookings);
        }
    }
}
