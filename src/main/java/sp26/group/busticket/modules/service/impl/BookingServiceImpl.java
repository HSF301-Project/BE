package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.account.response.UserProfileDTO;
import sp26.group.busticket.modules.dto.booking.request.BookingFormDTO;
import sp26.group.busticket.modules.dto.booking.request.PassengerInfoDTO;
import sp26.group.busticket.modules.dto.booking.response.MyTripResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PaymentResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.PriceItemDTO;
import sp26.group.busticket.modules.dto.booking.response.TicketConfirmationDTO;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.repository.*;
import sp26.group.busticket.modules.service.BookingService;

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
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public UUID createBooking(UUID tripId, BookingFormDTO form, Account currentAccount) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        BigDecimal totalAmount = trip.getPriceBase().multiply(BigDecimal.valueOf(form.getPassengers().size()));

        Booking booking = Booking.builder()
                .user(currentAccount)
                .trip(trip)
                .totalAmount(totalAmount)
                .status("PENDING")
                .build();
        booking = bookingRepository.save(booking);

        for (PassengerInfoDTO p : form.getPassengers()) {
            Seat seat = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId()).stream()
                    .filter(s -> s.getSeatNumber().equals(p.getSeatId()))
                    .findFirst()
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

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
    public PaymentResponseDTO getPaymentInfo(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow();
        Trip trip = booking.getTrip();
        Locale localeVN = new Locale("vi", "VN");
        NumberFormat vnFormat = NumberFormat.getCurrencyInstance(localeVN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return PaymentResponseDTO.builder()
                .bookingId(bookingId)
                .holdSecondsRemaining(600) // 10 minutes
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
        booking.setStatus("PAID");
        bookingRepository.save(booking);
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
                .qrImageUrl("https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=" + ticket.getTicketCode())
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
                    String status = "UPCOMING";
                    if (b.getStatus().equals("CANCELLED")) status = "CANCELLED";
                    else if (trip.getArrivalTime().isBefore(now)) status = "COMPLETED";

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
                .filter(dto -> tab == null || tab.equalsIgnoreCase("all") || dto.getStatus().equalsIgnoreCase(tab))
                .collect(Collectors.toList());
    }

    @Override
    public UserProfileDTO getUserProfile(Account account) {
        return UserProfileDTO.builder()
                .fullName(account.getFullName())
                .email(account.getEmail())
                .avatarUrl(null)
                .membershipTier("Vàng")
                .totalTrips(42)
                .membershipLabel("Premium Voyager")
                .build();
    }
}
