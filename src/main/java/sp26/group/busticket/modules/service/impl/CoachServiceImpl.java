package sp26.group.busticket.modules.service.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachDetailResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.TripHistoryDTO;
import sp26.group.busticket.modules.dto.trip.response.AdminSeatStatusDTO;
import sp26.group.busticket.modules.dto.trip.response.AdminTripDetailResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.entity.Coach;
import sp26.group.busticket.modules.entity.Seat;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.mapper.CoachMapper;
import sp26.group.busticket.modules.repository.CoachRepository;
import sp26.group.busticket.modules.repository.SeatRepository;
import sp26.group.busticket.modules.service.BookingService;
import sp26.group.busticket.modules.service.CoachService;
import sp26.group.busticket.modules.service.RouteService;
import sp26.group.busticket.modules.service.TripService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final SeatRepository seatRepository;
    private final CoachMapper coachMapper;

    private final TripService tripService;
    private final BookingService bookingService;
    private final RouteService routeService;

    public CoachServiceImpl(CoachRepository coachRepository,
                            SeatRepository seatRepository,
                            CoachMapper coachMapper,
                            @Lazy TripService tripService,
                            BookingService bookingService,
                            RouteService routeService) {
        this.coachRepository = coachRepository;
        this.seatRepository = seatRepository;
        this.coachMapper = coachMapper;
        this.tripService = tripService;
        this.bookingService = bookingService;
        this.routeService = routeService;
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    // ==================== COACH CRUD ====================

    @Override
    public CoachResponseDTO createCoach(CoachRequestDTO request) {
        if (coachRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.PLATE_NUMBER_ALREADY_EXISTS);
        }
        Coach coach = coachMapper.toEntity(request);
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    public List<CoachResponseDTO> getAllCoaches() {
        return coachRepository.findAll().stream()
                .map(coachMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CoachResponseDTO getCoachById(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));
        return coachMapper.toResponse(coach);
    }

    @Override
    public CoachResponseDTO updateCoach(UUID id, CoachRequestDTO request) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));

        coachRepository.findByPlateNumber(request.getPlateNumber())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new AppException(ErrorCode.PLATE_NUMBER_ALREADY_EXISTS);
                    }
                });

        coachMapper.updateEntity(coach, request);
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    public void deleteCoach(UUID id) {
        if (!coachRepository.existsById(id)) {
            throw new AppException(ErrorCode.COACH_NOT_FOUND);
        }

        if (bookingService.existsByCoachId(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT,
                    "Không thể xóa xe này vì đã có hành khách đặt vé. Vui lòng kiểm tra lại!");
        }

        if (tripService.existsByCoachId(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT,
                    "Không thể xóa xe này vì đang có chuyến đi liên quan!");
        }

        coachRepository.deleteById(id);
    }

    // ==================== COACH DETAIL ====================

    @Override
    public CoachDetailResponseDTO getCoachDetail(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));

        List<Trip> trips = tripService.findAllTripsByCoach(id);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        Trip latestTrip = trips.stream()
                .sorted((t1, t2) -> t2.getDepartureTime().compareTo(t1.getDepartureTime()))
                .findFirst()
                .orElse(null);

        String driverName = (latestTrip != null && latestTrip.getDriver() != null)
                ? latestTrip.getDriver().getFullName() : "Chưa phân công";
        String driverPhone = (latestTrip != null && latestTrip.getDriver() != null)
                ? latestTrip.getDriver().getPhone() : "N/A";

        List<TripHistoryDTO> history = trips.stream()
                .sorted((t1, t2) -> t2.getDepartureTime().compareTo(t1.getDepartureTime()))
                .map(trip -> TripHistoryDTO.builder()
                        .tripId(trip.getId())
                        .routeName(trip.getRoute().getDepartureLocation().getName() + " - " +
                                   trip.getRoute().getArrivalLocation().getName())
                        .departureTime(trip.getDepartureTime().format(formatter))
                        .arrivalTime(trip.getArrivalTime().format(formatter))
                        .status(trip.getTripStatus().name())
                        .driverName(trip.getDriver() != null ? trip.getDriver().getFullName() : "N/A")
                        .totalOccupiedSeats(bookingService.countBookedSeats(trip.getId()))
                        .build())
                .collect(Collectors.toList());

        return CoachDetailResponseDTO.builder()
                .id(coach.getId())
                .plateNumber(coach.getPlateNumber())
                .coachType(coach.getCoachType())
                .totalSeats(coach.getTotalSeats())
                .currentDriverName(driverName)
                .currentDriverPhone(driverPhone)
                .tripHistory(history)
                .build();
    }

    // ==================== TRIP DETAIL (Sơ đồ ghế) ====================

    @Override
    public AdminTripDetailResponseDTO getAdminTripDetail(UUID tripId) {
        Trip trip = tripService.findTripEntityById(tripId);

        Coach coach = trip.getCoach();
        List<Seat> allSeats = seatRepository.findByCoach_IdOrderBySeatNumberAsc(coach.getId());
        List<AdminSeatStatusDTO> occupiedSeats = bookingService.getSeatStatuses(tripId);

        List<AdminSeatStatusDTO> seatStatuses = allSeats.stream()
                .map(seat -> {
                    AdminSeatStatusDTO occupied = occupiedSeats.stream()
                            .filter(os -> os.getSeatNumber().equals(seat.getSeatNumber()))
                            .findFirst()
                            .orElse(null);

                    if (occupied != null) return occupied;

                    return AdminSeatStatusDTO.builder()
                            .seatNumber(seat.getSeatNumber())
                            .floor(seat.getFloor())
                            .isOccupied(false)
                            .build();
                })
                .collect(Collectors.toList());

        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateOnly = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return AdminTripDetailResponseDTO.builder()
                .tripId(trip.getId())
                .coachPlate(coach.getPlateNumber())
                .coachType(coach.getCoachType())
                .routeName(trip.getRoute().getRouteCode())
                .departureTime(trip.getDepartureTime().format(timeOnly))
                .arrivalTime(trip.getArrivalTime().format(timeOnly))
                .departureDate(trip.getDepartureTime().format(dateOnly))
                .driverName(trip.getDriver() != null ? trip.getDriver().getFullName() : "N/A")
                .driverPhone(trip.getDriver() != null ? trip.getDriver().getPhone() : "N/A")
                .assistantName(trip.getAssistant() != null ? trip.getAssistant().getFullName() : "N/A")
                .assistantPhone(trip.getAssistant() != null ? trip.getAssistant().getPhone() : "N/A")
                .seats(seatStatuses)
                .stopEtas(routeService.buildRouteTimeline(trip, timeOnly))
                .build();
    }

    // ==================== HELPER ====================

    @Override
    public Coach getCoachEntityById(UUID id) {
        return coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));
    }

    @Override
    public boolean existsById(UUID id) {
        return coachRepository.existsById(id);
    }
}
