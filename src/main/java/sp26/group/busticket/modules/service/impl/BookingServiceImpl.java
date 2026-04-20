package sp26.group.busticket.modules.service.impl;

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
import sp26.group.busticket.modules.dto.booking.response.MyTripResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PaymentResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PriceItemDTO;
import sp26.group.busticket.modules.dto.booking.response.TicketConfirmationDTO;
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
    private final PasswordEncoder passwordEncoder;

    private static final int CLEANUP_MINUTES = 7;

    @Override
    @Transactional
    public UUID createBooking(UUID tripId, BookingFormDTO form, Account currentAccount) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

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

        Booking booking = Booking.builder()
                .user(currentAccount)
                .trip(trip)
                .totalAmount(totalAmount)
                .status(BookingStatusEnum.PENDING)
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
        Booking booking = Booking.builder()
                .user(userAccount) // Dùng account tìm được
                .createdBy(staffAccount)
                .trip(trip)
                .totalAmount(totalAmount)
                .status(BookingStatusEnum.CONFIRMED)
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
    public PaymentResponseDTO getPaymentInfo(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Trip trip = booking.getTrip();
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return PaymentResponseDTO.builder()
                .bookingId(bookingId)
                .expiryTime(booking.getCreatedAt().plusMinutes(CLEANUP_MINUTES).format(timeFormatter))
                .fromCity(trip.getRoute().getDepartureLocation().getName())
                .toCity(trip.getRoute().getArrivalLocation().getName())
                .departureTime(trip.getDepartureTime().format(timeFormatter))
                .arrivalTime(trip.getArrivalTime().format(timeFormatter))
                .dateLabel(trip.getDepartureTime().format(dateFormatter))
                .busTypeLabel(trip.getCoach().getCoachType())
                .ticketCount((int) ticketRepository.findByBooking_Trip_Id(trip.getId()).stream().filter(t -> t.getBooking().getId().equals(bookingId)).count())
                .totalFormatted(vnFormat.format(booking.getTotalAmount()))
                .build();
    }

    @Override
    @Transactional
    public void processPayment(UUID bookingId, String paymentMethod) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        
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
    public TicketConfirmationDTO getBookingSuccessInfo(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Ticket ticket = ticketRepository.findAll().stream()
                .filter(t -> t.getBooking().getId().equals(bookingId))
                .findFirst().orElseThrow();
        Trip trip = booking.getTrip();
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", localeVN);

        return TicketConfirmationDTO.builder()
                .id(ticket.getId())
                .statusLabel("Đã xác nhận")
                .bookingCode(ticket.getTicketCode())
                .fromCityShort(trip.getRoute().getDepartureLocation().getName().toUpperCase())
                .toCityShort(trip.getRoute().getArrivalLocation().getName().toUpperCase())
                .departureStation(trip.getRoute().getDepartureLocation().getName())
                .arrivalStation(trip.getRoute().getArrivalLocation().getName())
                .departureTime(trip.getDepartureTime().format(timeFormatter))
                .arrivalTime(trip.getArrivalTime().format(timeFormatter))
                .departureDateLabel(trip.getDepartureTime().format(dateFormatter))
                .seatLabel(ticket.getSeat().getSeatNumber() + " (Tầng " + ticket.getSeat().getFloor() + ")")
                .licensePlate(trip.getCoach().getPlateNumber())
                .serviceType(trip.getCoach().getCoachType())
                .passengerName(ticket.getPassengerName())
                .totalFormatted(vnFormat.format(booking.getTotalAmount()))
                .build();
    }

    @Override
    public List<MyTripResponseDTO> getMyTrips(UUID accountId, String tab) {
        List<Booking> bookings = bookingRepository.findByUser_IdOrderByCreatedAtDesc(accountId);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime now = LocalDateTime.now();

        return bookings.stream()
                .map(b -> {
                    Trip trip = b.getTrip();
                    BookingStatusEnum status = b.getStatus();
                    
                    // Nếu đã thanh toán nhưng chuyến đi đã kết thúc thì coi là COMPLETED
                    if (status == BookingStatusEnum.CONFIRMED && trip.getArrivalTime().isBefore(now)) {
                        status = BookingStatusEnum.COMPLETED;
                    }

                    return MyTripResponseDTO.builder()
                            .id(b.getId())
                            .bookingCode(ticketRepository.findAll().stream()
                                    .filter(t -> t.getBooking().getId().equals(b.getId()))
                                    .findFirst().map(Ticket::getTicketCode).orElse("N/A"))
                            .status(status)
                            .busTypeLabel(trip.getCoach().getCoachType())
                            .daysUntilDeparture(java.time.Duration.between(now, trip.getDepartureTime()).toDays())
                            .fromCity(trip.getRoute().getDepartureLocation().getName())
                            .departureStation(trip.getRoute().getDepartureLocation().getName())
                            .departureTime(trip.getDepartureTime().format(timeFormatter))
                            .toCity(trip.getRoute().getArrivalLocation().getName())
                            .arrivalStation(trip.getRoute().getArrivalLocation().getName())
                            .arrivalTime(trip.getArrivalTime().format(timeFormatter))
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
