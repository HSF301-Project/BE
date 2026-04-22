package sp26.group.busticket.modules.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.coach.request.CoachRequestDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.CoachDetailResponseDTO;
import sp26.group.busticket.modules.dto.coach.response.PassengerDetailDTO;
import sp26.group.busticket.modules.dto.coach.response.TripDetailDTO;
import sp26.group.busticket.modules.dto.coach.response.TripHistoryDTO;
import sp26.group.busticket.modules.dto.trip.response.AdminSeatStatusDTO;
import sp26.group.busticket.modules.dto.trip.response.AdminTripDetailResponseDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.entity.Coach;
import sp26.group.busticket.modules.entity.Seat;
import sp26.group.busticket.modules.entity.Ticket;
import sp26.group.busticket.modules.mapper.CoachMapper;
import sp26.group.busticket.modules.repository.CoachRepository;
import sp26.group.busticket.modules.repository.SeatRepository;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.CoachService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final TripRepository tripRepository;
    private final TicketRepository ticketRepository;
    private final SeatRepository seatRepository;
    private final CoachMapper coachMapper;
    private final sp26.group.busticket.modules.repository.CoachTypeRepository coachTypeRepository;

    @Override
    @Transactional
    public CoachResponseDTO createCoach(CoachRequestDTO request) {
        if (coachRepository.findByPlateNumber(request.getPlateNumber()).isPresent()) {
            throw new AppException(ErrorCode.PLATE_NUMBER_ALREADY_EXISTS);
        }
        Coach coach = coachMapper.toEntity(request);
        coach.setCoachType(coachTypeRepository.findById(request.getCoachTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy loại xe yêu cầu")));
        coach.setStatus(sp26.group.busticket.modules.enumType.CoachStatusEnum.AVAILABLE);
        Coach savedCoach = coachRepository.save(coach);
        
        generateSeatsForCoach(savedCoach);
        
        return enrichCoachResponse(savedCoach);
    }

    @Override
    public List<CoachResponseDTO> getAllCoaches() {
        return coachRepository.findAll().stream()
                .map(this::enrichCoachResponse)
                .collect(Collectors.toList());
    }

    private CoachResponseDTO enrichCoachResponse(Coach coach) {
        CoachResponseDTO dto = coachMapper.toResponse(coach);
        dto.setStatus(coach.getStatus()); 
        dto.setStatusLabel(translateCoachStatus(coach.getStatus()));
        return dto;
    }

    @Override
    public CoachResponseDTO getCoachById(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));
        return enrichCoachResponse(coach);
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
        coach.setCoachType(coachTypeRepository.findById(request.getCoachTypeId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_INPUT, "Không tìm thấy loại xe yêu cầu")));
        return coachMapper.toResponse(coachRepository.save(coach));
    }

    @Override
    public void deleteCoach(UUID id) {
        if (!coachRepository.existsById(id)) {
            throw new AppException(ErrorCode.COACH_NOT_FOUND);
        }
        
        // Kiểm tra xem xe có khách hàng đã đặt vé không
        if (ticketRepository.existsBySeat_Coach_Id(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể xóa xe này vì đã có hành khách đặt vé trên các chỗ ngồi của xe. Vui lòng kiểm tra lại!");
        }

        // Kiểm tra xem xe có đang nằm trong chuyến đi nào không
        if (tripRepository.existsByCoach_Id(id)) {
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể xóa xe này vì đang có chuyến đi liên quan!");
        }
        
        coachRepository.deleteById(id);
    }

    @Override
    public CoachDetailResponseDTO getCoachDetails(UUID id) {
        Coach coach = coachRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND));

        List<sp26.group.busticket.modules.entity.Trip> trips = tripRepository.findAllTripsByCoach(id);
        
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        // Tìm tài xế hiện tại (Lấy từ chuyến gần nhất chưa kết thúc hoặc chuyến mới nhất)
        sp26.group.busticket.modules.entity.Trip latestTrip = trips.stream()
                .sorted((t1, t2) -> t2.getDepartureTime().compareTo(t1.getDepartureTime()))
                .findFirst()
                .orElse(null);

        String driverName = (latestTrip != null && latestTrip.getDriver() != null) ? latestTrip.getDriver().getFullName() : "Chưa phân công";
        String driverPhone = (latestTrip != null && latestTrip.getDriver() != null) ? latestTrip.getDriver().getPhone() : "N/A";

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
                        .totalOccupiedSeats(ticketRepository.findByBooking_Trip_Id(trip.getId()).size())
                        .build())
                .collect(Collectors.toList());

        return CoachDetailResponseDTO.builder()
                .id(coach.getId())
                .plateNumber(coach.getPlateNumber())
                .coachType(coach.getCoachType().getName())
                .totalSeats(coach.getTotalSeats())
                .currentDriverName(driverName)
                .currentDriverPhone(driverPhone)
                .tripHistory(history)
                .build();
    }

    @Override
    public AdminTripDetailResponseDTO getAdminTripDetail(UUID tripId) {
        sp26.group.busticket.modules.entity.Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        Coach coach = trip.getCoach();
        List<Seat> allSeats = seatRepository.findByCoach_IdOrderBySeatNumberAsc(coach.getId());
        if (allSeats.isEmpty() && coach.getTotalSeats() != null && coach.getTotalSeats() > 0) {
            generateSeatsForCoach(coach);
            allSeats = seatRepository.findByCoach_IdOrderBySeatNumberAsc(coach.getId());
        }
        List<Ticket> tickets = ticketRepository.findByBooking_Trip_Id(tripId);

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        java.time.format.DateTimeFormatter timeOnly = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        java.time.format.DateTimeFormatter dateOnly = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

        List<AdminSeatStatusDTO> seatStatuses = allSeats.stream()
                .map(seat -> {
                    Ticket ticket = tickets.stream()
                            .filter(t -> t.getSeat().getId().equals(seat.getId()))
                            .findFirst()
                            .orElse(null);

                    return AdminSeatStatusDTO.builder()
                            .seatNumber(seat.getSeatNumber())
                            .floor(seat.getFloor())
                            .isOccupied(ticket != null)
                            .passengerName(ticket != null ? ticket.getPassengerName() : null)
                            .passengerPhone(ticket != null ? ticket.getPassengerPhone() : null)
                            .ticketCode(ticket != null ? ticket.getTicketCode() : null)
                            .build();
                })
                .collect(Collectors.toList());

        List<TripStopEtaDTO> stopEtas = buildStopEtas(trip, timeOnly);
        String intermediateStopsText = stopEtas.stream()
                .filter(s -> "INTERMEDIATE".equals(s.getStopType()))
                .map(TripStopEtaDTO::getStopName)
                .collect(Collectors.joining(", "));

        return AdminTripDetailResponseDTO.builder()
                .tripId(tripId)
                .routeName(trip.getRoute().getDepartureLocation().getName() + " - " + 
                           trip.getRoute().getArrivalLocation().getName())
                .departureTime(trip.getDepartureTime().format(formatter))
                .arrivalTime(trip.getArrivalTime().format(formatter))
                .departureDate(trip.getDepartureTime().format(dateOnly))
                .coachPlate(coach.getPlateNumber())
                .coachType(coach.getCoachType().getName())
                .driverName(trip.getDriver() != null ? trip.getDriver().getFullName() : "N/A")
                .driverPhone(trip.getDriver() != null ? trip.getDriver().getPhone() : "N/A")
                .assistantName(trip.getAssistant() != null ? trip.getAssistant().getFullName() : "Chưa phân công")
                .assistantPhone(trip.getAssistant() != null ? trip.getAssistant().getPhone() : "N/A")
                .pickUpAddress(trip.getRoute().getDepartureLocation().getName() + ", " + trip.getRoute().getDepartureLocation().getCity())
                .dropOffAddress(trip.getRoute().getArrivalLocation().getName() + ", " + trip.getRoute().getArrivalLocation().getCity())
                .intermediateStopsText(intermediateStopsText.isBlank() ? "Không có" : intermediateStopsText)
                .stopEtas(stopEtas)
                .seats(seatStatuses)
                .build();
    }

    private List<TripStopEtaDTO> buildStopEtas(sp26.group.busticket.modules.entity.Trip trip,
                                               java.time.format.DateTimeFormatter timeOnly) {
        // Build stop list from RouteStop entities (FK → Location)
        List<StopWithKm> stops = new ArrayList<>();
        stops.add(new StopWithKm(trip.getRoute().getDepartureLocation().getName(), 0f, "START"));

        if (trip.getRoute().getStops() != null) {
            for (sp26.group.busticket.modules.entity.RouteStop rs : trip.getRoute().getStops()) {
                stops.add(new StopWithKm(
                        rs.getLocation().getName(),
                        rs.getDistanceFromStart(),
                        "INTERMEDIATE"));
            }
        }

        Float totalKm = trip.getRoute().getDistance();
        if (totalKm == null || totalKm <= 0) {
            totalKm = 1f;
        }
        stops.add(new StopWithKm(trip.getRoute().getArrivalLocation().getName(), totalKm, "END"));

        // Prefer distance-based ETA when km is present, else fallback to equal distribution for those without km.
        long totalMinutes = java.time.Duration.between(trip.getDepartureTime(), trip.getArrivalTime()).toMinutes();
        if (totalMinutes <= 0 && trip.getRoute().getDuration() != null) {
            totalMinutes = trip.getRoute().getDuration();
        }
        if (totalMinutes <= 0) {
            totalMinutes = 60;
        }

        List<StopWithKm> withKm = stops.stream().filter(s -> s.km != null).sorted(Comparator.comparing(s -> s.km)).toList();
        // If route dataset is present, at least START and END have km.
        boolean canUseKm = withKm.size() >= 2;

        List<TripStopEtaDTO> result = new ArrayList<>();
        if (canUseKm) {
            for (StopWithKm s : stops) {
                long offsetMinutes;
                if (s.km != null) {
                    offsetMinutes = Math.round(totalMinutes * (s.km / totalKm));
                } else {
                    // fallback: put unknown-km stops evenly between START and END
                    int idx = stops.indexOf(s);
                    int segments = Math.max(stops.size() - 1, 1);
                    offsetMinutes = Math.round((double) idx * totalMinutes / segments);
                }
                LocalDateTime eta = trip.getDepartureTime().plusMinutes(offsetMinutes);
                result.add(TripStopEtaDTO.builder()
                        .stopName(s.name)
                        .etaTime(eta.format(timeOnly))
                        .stopType(s.type)
                        .pointType("BOTH")
                        .pointTypeLabel("Đón & trả")
                        .offsetMinutes((int) offsetMinutes)
                        .build());
            }
            return result;
        }

        // Pure fallback: equal distribution
        int segments = Math.max(stops.size() - 1, 1);
        for (int i = 0; i < stops.size(); i++) {
            long offsetMinutes = Math.round((double) i * totalMinutes / segments);
            LocalDateTime eta = trip.getDepartureTime().plusMinutes(offsetMinutes);
            StopWithKm s = stops.get(i);
            result.add(TripStopEtaDTO.builder()
                    .stopName(s.name)
                    .etaTime(eta.format(timeOnly))
                    .stopType(s.type)
                    .pointType("BOTH")
                    .pointTypeLabel("Đón & trả")
                    .offsetMinutes((int) offsetMinutes)
                    .build());
        }
        return result;
    }

    private static class StopWithKm {
        final String name;
        final Float km;
        final String type;

        private StopWithKm(String name, Float km, String type) {
            this.name = name;
            this.km = km;
            this.type = type;
        }
    }
    private void generateSeatsForCoach(Coach coach) {
        int total = coach.getTotalSeats();
        int seatsPerFloor = total;
        int floors = 1;

        String type = coach.getCoachType().getName().toUpperCase();
        // Heuristic: Sleeper buses or high seat count (usually 2 floors in VN)
        if (type.contains("GIƯỜNG") || type.contains("SLEEPER") || type.contains("CABIN") || 
            (total > 22 && total <= 46 && !type.contains("GHẾ"))) {
            floors = 2;
            seatsPerFloor = (int) Math.ceil((double) total / 2);
        }

        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= total; i++) {
            int currentFloor = (i <= seatsPerFloor) ? 1 : 2;
            int numberInFloor = (currentFloor == 1) ? i : i - seatsPerFloor;
            String prefix = (currentFloor == 1) ? "A" : "B";
            String seatNumber = prefix + String.format("%02d", numberInFloor);

            seats.add(Seat.builder()
                    .coach(coach)
                    .seatNumber(seatNumber)
                    .floor(currentFloor)
                    .build());
        }
        seatRepository.saveAll(seats);
    }

    private String translateCoachStatus(sp26.group.busticket.modules.enumType.CoachStatusEnum status) {
        if (status == null) return "N/A";
        return switch (status) {
            case AVAILABLE -> "Sẵn sàng";
            case MAINTENANCE -> "Bảo trì";
            case WORKING -> "Đang vận hành";
            case INACTIVE -> "Ngừng hoạt động";
            default -> "Không xác định";
        };
    }
}
