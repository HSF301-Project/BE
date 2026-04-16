package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.booking.request.BookingRequestDTO;
import sp26.group.busticket.modules.dto.booking.response.AvailableSeatsResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.BookingResponseDTO;
import sp26.group.busticket.modules.dto.booking.response.TripDetailResponseDTO;
import sp26.group.busticket.modules.entity.*;
import sp26.group.busticket.modules.repository.*;
import sp26.group.busticket.modules.service.BookingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final TripRepository tripRepository;
    private final BookingRepository bookingRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;

    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public TripDetailResponseDTO getTripDetails(Integer tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Trip not found"));

        int availableSeats = seatRepository.findAvailableSeatsByTripId(
                trip.getCoach().getId(), tripId).size();

        return TripDetailResponseDTO.builder()
                .tripId(trip.getId())
                .departureLocation(trip.getRoute().getDepartureLocation().getName())
                .arrivalLocation(trip.getRoute().getArrivalLocation().getName())
                .departureTime(trip.getDepartureTime().format(DATETIME_FORMAT))
                .arrivalTime(trip.getArrivalTime().format(DATETIME_FORMAT))
                .priceBase(trip.getPriceBase())
                .coachType(trip.getCoach().getCoachType())
                .plateNumber(trip.getCoach().getPlateNumber())
                .totalSeats(trip.getCoach().getTotalSeats())
                .availableSeats(availableSeats)
                .build();
    }

    @Override
    public AvailableSeatsResponseDTO getAvailableSeats(Integer tripId) {
        TripEntity trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Trip not found"));

        List<SeatEntity> availableSeats = seatRepository.findAvailableSeatsByTripId(
                trip.getCoach().getId(), tripId);

        List<AvailableSeatsResponseDTO.SeatDTO> seatDTOs = availableSeats.stream()
                .map(seat -> AvailableSeatsResponseDTO.SeatDTO.builder()
                        .seatId(seat.getId())
                        .seatNumber(seat.getSeatNumber())
                        .floor(seat.getFloor())
                        .build())
                .collect(Collectors.toList());

        return AvailableSeatsResponseDTO.builder()
                .tripId(tripId)
                .coachId(trip.getCoach().getId())
                .coachType(trip.getCoach().getCoachType())
                .plateNumber(trip.getCoach().getPlateNumber())
                .seats(seatDTOs)
                .build();
    }

    @Override
    @Transactional
    public BookingResponseDTO createBooking(AccountEntity user, BookingRequestDTO request) {
        TripEntity trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Trip not found"));

        List<TicketEntity> tickets = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (BookingRequestDTO.PassengerInfoDTO passenger : request.getPassengers()) {
            SeatEntity seat = seatRepository.findById(passenger.getSeatId())
                    .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Seat not found"));

            boolean isAvailable = seatRepository.findAvailableSeatsByTripId(
                    trip.getCoach().getId(), request.getTripId())
                    .stream()
                    .anyMatch(s -> s.getId().equals(seat.getId()));

            if (!isAvailable) {
                throw new AppException(ErrorCode.BAD_REQUEST, "Seat " + seat.getSeatNumber() + " is not available");
            }

            TicketEntity ticket = TicketEntity.builder()
                    .seat(seat)
                    .passengerName(passenger.getPassengerName())
                    .ticketCode(generateTicketCode())
                    .build();
            tickets.add(ticket);
            totalAmount = totalAmount.add(trip.getPriceBase());
        }

        BookingEntity booking = BookingEntity.builder()
                .user(user)
                .trip(trip)
                .totalAmount(totalAmount)
                .status("PENDING")
                .createdAt(java.time.LocalDateTime.now())
                .build();
        booking = bookingRepository.save(booking);

        for (TicketEntity ticket : tickets) {
            ticket.setBooking(booking);
        }
        ticketRepository.saveAll(tickets);

        PaymentEntity payment = PaymentEntity.builder()
                .booking(booking)
                .paymentMethod(request.getPaymentMethod())
                .amount(totalAmount)
                .status("PENDING")
                .transactionId(generateTransactionId())
                .build();
        payment = paymentRepository.save(payment);

        return mapToBookingResponse(booking, tickets, payment);
    }

    @Override
    @Transactional
    public BookingResponseDTO processPayment(Integer bookingId, String paymentMethod) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking not found"));

        if (!"PENDING".equals(booking.getStatus())) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Booking already processed");
        }

        PaymentEntity payment = paymentRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Payment not found"));

        payment.setPaymentMethod(paymentMethod);
        payment.setStatus("COMPLETED");
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);

        List<TicketEntity> tickets = ticketRepository.findByBookingId(bookingId);
        return mapToBookingResponse(booking, tickets, payment);
    }

    @Override
    public BookingResponseDTO getBookingDetails(Integer bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND, "Booking not found"));

        List<TicketEntity> tickets = ticketRepository.findByBookingId(bookingId);
        PaymentEntity payment = paymentRepository.findByBookingId(bookingId).orElse(null);

        return mapToBookingResponse(booking, tickets, payment);
    }

    @Override
    public List<BookingResponseDTO> getBookingsByPhone(String phone) {
        List<BookingEntity> bookings = bookingRepository.findByUserPhone(phone);
        
        return bookings.stream()
                .map(booking -> {
                    List<TicketEntity> tickets = ticketRepository.findByBookingId(booking.getId());
                    PaymentEntity payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
                    return mapToBookingResponse(booking, tickets, payment);
                })
                .collect(Collectors.toList());
    }

    private BookingResponseDTO mapToBookingResponse(BookingEntity booking, List<TicketEntity> tickets, PaymentEntity payment) {
        TripEntity trip = booking.getTrip();
        
        List<BookingResponseDTO.TicketInfoDTO> ticketDTOs = tickets.stream()
                .map(t -> BookingResponseDTO.TicketInfoDTO.builder()
                        .ticketId(t.getId())
                        .ticketCode(t.getTicketCode())
                        .seatNumber(t.getSeat().getSeatNumber())
                        .floor(t.getSeat().getFloor())
                        .passengerName(t.getPassengerName())
                        .build())
                .collect(Collectors.toList());

        return BookingResponseDTO.builder()
                .bookingId(booking.getId())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .trip(BookingResponseDTO.TripInfoDTO.builder()
                        .tripId(trip.getId())
                        .departureLocation(trip.getRoute().getDepartureLocation().getName())
                        .arrivalLocation(trip.getRoute().getArrivalLocation().getName())
                        .departureTime(trip.getDepartureTime().format(DATETIME_FORMAT))
                        .arrivalTime(trip.getArrivalTime().format(DATETIME_FORMAT))
                        .coachType(trip.getCoach().getCoachType())
                        .plateNumber(trip.getCoach().getPlateNumber())
                        .build())
                .tickets(ticketDTOs)
                .payment(payment != null ? BookingResponseDTO.PaymentInfoDTO.builder()
                        .paymentId(payment.getId())
                        .paymentMethod(payment.getPaymentMethod())
                        .amount(payment.getAmount())
                        .status(payment.getStatus())
                        .transactionId(payment.getTransactionId())
                        .build() : null)
                .build();
    }

    private String generateTicketCode() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}
