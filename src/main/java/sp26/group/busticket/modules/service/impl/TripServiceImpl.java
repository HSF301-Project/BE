package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.trip.TripAdminConstants;
import sp26.group.busticket.modules.dto.trip.TripPageResponse;
import sp26.group.busticket.modules.dto.trip.TripAdminResponseDTO;
import sp26.group.busticket.modules.dto.trip.TripStatsResponseDTO;
import sp26.group.busticket.modules.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripDriverOptionDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.mapper.TripMapper;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final PasswordEncoder passwordEncoder;
    
    private final RouteService routeService;
    private final CoachService coachService;
    private final AccountService accountService;
    private final BookingService bookingService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    @Override
    public TripSearchResultDTO searchTrips(TripSearchRequestDTO request) {
        LocalDate date = (request.getDate() != null && !request.getDate().isBlank())
                ? LocalDate.parse(request.getDate())
                : LocalDate.now();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime searchStart = date.equals(now.toLocalDate()) ? now : date.atStartOfDay();
        LocalDateTime searchEnd = date.atTime(LocalTime.MAX);

        List<Trip> trips = tripRepository.findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
                request.getFrom(), request.getTo(), searchStart, searchEnd);

        List<sp26.group.busticket.modules.dto.trip.response.TripResponseDTO> tripDTOs = trips.stream()
                .filter(t -> request.getBusType() == null || request.getBusType().isEmpty() ||
                        t.getCoach().getCoachType().equalsIgnoreCase(request.getBusType()))
                .filter(t -> request.getMaxPrice() == null ||
                        t.getPriceBase().compareTo(BigDecimal.valueOf(request.getMaxPrice())) <= 0)
                .map(this::mapToClientResponseDTO)
                .filter(dto -> dto.getSeatsLeft() != null && dto.getSeatsLeft() > 0)
                .collect(Collectors.toList());

        return TripSearchResultDTO.builder()
                .fromCity(request.getFrom())
                .toCity(request.getTo())
                .date(date.toString())
                .dateLabel(date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")))
                .trips(tripDTOs)
                .totalCount((long) tripDTOs.size())
                .build();
    }

    @Override
    public BigDecimal getBasePriceByTripId(UUID tripId) {
        return tripRepository.findById(tripId)
                .map(Trip::getPriceBase)
                .orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public TripPageResponse getAdminDashboardData(String query, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("departureTime").descending());
        
        TripStatusEnum statusEnum = null;
        if (status != null && !status.equalsIgnoreCase("ALL")) {
            try {
                statusEnum = TripStatusEnum.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Ignore invalid status
            }
        }

        Page<Trip> tripPage;
        if (StringUtils.hasText(query)) {
            if (statusEnum != null) {
                tripPage = tripRepository.findByRoute_DepartureLocation_NameContainingOrRoute_ArrivalLocation_NameContainingAndTripStatus(query, query, statusEnum, pageable);
            } else {
                tripPage = tripRepository.findByRoute_DepartureLocation_NameContainingOrRoute_ArrivalLocation_NameContaining(query, query, pageable);
            }
        } else {
            if (statusEnum != null) {
                tripPage = tripRepository.findByTripStatus(statusEnum, pageable);
            } else {
                tripPage = tripRepository.findAll(pageable);
            }
        }

        List<TripAdminResponseDTO> tripDTOs = tripPage.getContent().stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());

        return TripPageResponse.builder()
                .trips(tripDTOs)
                .stats(computeStats())
                .currentPage(page)
                .totalPages(tripPage.getTotalPages())
                .totalCount(tripPage.getTotalElements())
                .displayedCount(tripDTOs.size())
                .hasNext(tripPage.hasNext())
                .build();
    }

    @Override
    public TripAdminResponseDTO getTripById(UUID id) {
        return tripRepository.findById(id)
                .map(this::mapToAdminResponseDTO)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
    }

    @Override
    public TripRequestDTO getTripForEdit(UUID id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        return TripRequestDTO.builder()
                .id(trip.getId())
                .routeId(trip.getRoute().getId())
                .coachId(trip.getCoach().getId())
                .driverId(trip.getDriver() != null ? trip.getDriver().getId() : null)
                .assistantId(trip.getAssistant() != null ? trip.getAssistant().getId() : null)
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getArrivalTime())
                .priceBase(trip.getPriceBase())
                .contactPhoneNumber(trip.getContactPhoneNumber())
                .status(trip.getTripStatus())
                .driverInputMode("existing")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDriverOptionDTO> listAssignableDrivers() {
        return accountService.getAssistants().stream()
                .filter(a -> "DRIVER".equalsIgnoreCase(a.getRole()))
                .map(a -> TripDriverOptionDTO.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .licenseNumber(a.getDriverLicenseNumber())
                        .readinessLabel("Hoạt động — sẵn sàng nhận chuyến")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UUID> createTrip(TripRequestDTO req) {
        validateTripBusinessRules(req, null);
        boolean newDriverMode = StringUtils.hasText(req.getDriverInputMode()) 
                && "new".equalsIgnoreCase(req.getDriverInputMode().trim());
        Account driver = resolveDriverAccount(req, newDriverMode);
        Trip trip = Trip.builder()
                .route(routeService.getRouteEntityById(req.getRouteId()))
                .coach(coachService.getCoachEntityById(req.getCoachId()))
                .driver(driver)
                .assistant(req.getAssistantId() != null
                        ? accountService.findById(req.getAssistantId()).orElse(null)
                        : null)
                .departureTime(req.getDepartureTime())
                .arrivalTime(req.getArrivalTime())
                .priceBase(req.getPriceBase())
                .contactPhoneNumber(req.getContactPhoneNumber())
                .tripStatus(req.getStatus())
                .build();
        tripRepository.save(trip);
        return newDriverMode ? Optional.of(driver.getId()) : Optional.empty();
    }

    @Override
    @Transactional
    public Optional<UUID> updateTrip(UUID id, TripRequestDTO req) {
        validateTripBusinessRules(req, id);
        boolean newDriverMode = StringUtils.hasText(req.getDriverInputMode()) 
                && "new".equalsIgnoreCase(req.getDriverInputMode().trim());
        Account driver = resolveDriverAccount(req, newDriverMode);
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        trip.setRoute(routeService.getRouteEntityById(req.getRouteId()));
        trip.setCoach(coachService.getCoachEntityById(req.getCoachId()));
        trip.setDriver(driver);
        trip.setAssistant(req.getAssistantId() != null
                ? accountService.findById(req.getAssistantId()).orElse(null)
                : null);
        trip.setDepartureTime(req.getDepartureTime());
        trip.setArrivalTime(req.getArrivalTime());
        trip.setPriceBase(req.getPriceBase());
        trip.setContactPhoneNumber(req.getContactPhoneNumber());
        trip.setTripStatus(req.getStatus());

        tripRepository.save(trip);
        return newDriverMode ? Optional.of(driver.getId()) : Optional.empty();
    }

    @Override
    @Transactional
    public void deleteTrip(UUID id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        if (bookingService.countBookedSeats(id) > 0) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Không thể xóa chuyến đi đã có người đặt vé.");
        }
        tripRepository.delete(trip);
    }

    @Override
    public boolean existsByCoachId(UUID coachId) {
        return tripRepository.existsByCoach_Id(coachId);
    }

    @Override
    public List<Trip> findAllTripsByCoach(UUID coachId) {
        return tripRepository.findAllTripsByCoach(coachId);
    }

    @Override
    public Trip findTripEntityById(UUID id) {
        return tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
    }

    @Override
    public long countActiveTrips() {
        return tripRepository.countByTripStatus(TripStatusEnum.DEPARTED);
    }

    private void validateTripBusinessRules(TripRequestDTO req, UUID currentTripId) {
        if (req.getDepartureTime().isAfter(req.getArrivalTime())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thời gian khởi hành phải trước thời gian đến.");
        }
        
        List<Trip> coachTrips = tripRepository.findAllTripsByCoach(req.getCoachId());
        for (Trip existing : coachTrips) {
            if (currentTripId != null && existing.getId().equals(currentTripId)) continue;
            
            LocalDateTime eStart = existing.getDepartureTime();
            LocalDateTime eEnd = existing.getArrivalTime();
            LocalDateTime nStart = req.getDepartureTime();
            LocalDateTime nEnd = req.getArrivalTime();

            if (nStart.isBefore(eEnd.plusMinutes(60)) && nEnd.isAfter(eStart.minusMinutes(60))) {
                throw new AppException(ErrorCode.INVALID_INPUT, 
                    "Xung đột lịch trình: Xe đã có chuyến đi từ " + eStart.format(TIME_FORMATTER) + " đến " + eEnd.format(TIME_FORMATTER) + ". Cần ít nhất 60 phút nghỉ.");
            }

            if (nStart.isAfter(eEnd)) {
                String lastStop = existing.getRoute().getArrivalLocation().getName();
                String nextStart = routeService.getRouteEntityById(req.getRouteId()).getDepartureLocation().getName();
                if (!lastStop.equalsIgnoreCase(nextStart)) {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Xung đột vị trí: Xe đang ở " + lastStop + ", không thể khởi hành từ " + nextStart);
                }
            }
        }
    }

    private Account resolveDriverAccount(TripRequestDTO req, boolean newDriverMode) {
        if (newDriverMode) {
            String email = req.getNewDriverEmail().trim().toLowerCase();
            if (accountService.existsByEmail(email)) throw new AppException(ErrorCode.DRIVER_EMAIL_EXISTS);
            
            Account created = Account.builder()
                    .email(email)
                    .password(passwordEncoder.encode(TripAdminConstants.NEW_DRIVER_TEMP_PASSWORD))
                    .fullName(req.getNewDriverFullName().trim())
                    .phone(req.getNewDriverPhone().trim())
                    .driverLicenseNumber(req.getNewDriverLicense())
                    .status(StatusEnum.ACTIVE)
                    .role("DRIVER")
                    .build();
            return accountService.saveAccount(created);
        }
        return accountService.findById(req.getDriverId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private sp26.group.busticket.modules.dto.trip.response.TripResponseDTO mapToClientResponseDTO(Trip trip) {
        sp26.group.busticket.modules.dto.trip.response.TripResponseDTO dto = tripMapper.toClientTripResponseDTO(trip);
        dto.setName("Chuyến xe " + trip.getCoach().getPlateNumber());
        int booked = bookingService.countBookedSeats(trip.getId());
        int total = trip.getCoach().getTotalSeats() != null ? trip.getCoach().getTotalSeats() : 0;
        dto.setSeatsLeft(total - booked);
        dto.setPriceFormatted(NumberFormat.getCurrencyInstance(LOCALE_VN).format(trip.getPriceBase()));
        return dto;
    }

    private TripAdminResponseDTO mapToAdminResponseDTO(Trip trip) {
        TripAdminResponseDTO dto = tripMapper.toAdminTripResponseDTO(trip);
        dto.setTripCode("TRP-" + trip.getId().toString().substring(0, 8).toUpperCase());
        dto.setStatus(trip.getTripStatus() != null ? trip.getTripStatus().name() : null);
        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH:mm");
        int booked = bookingService.countBookedSeats(trip.getId());
        int total = (trip.getCoach() != null && trip.getCoach().getTotalSeats() != null) ? trip.getCoach().getTotalSeats() : 0;
        int fillPercent = (total > 0) ? (int) ((double) booked / total * 100) : 0;

        dto.setBookedSeats(booked);
        dto.setTotalSeats(total);
        dto.setFillPercent(fillPercent);
        dto.setRouteTimeline(routeService.buildRouteTimeline(trip, timeOnly));
        dto.setStatusLabel(translateStatus(trip.getTripStatus()));
        dto.setPriceFormatted(NumberFormat.getCurrencyInstance(LOCALE_VN).format(trip.getPriceBase()));
        return dto;
    }

    private TripStatsResponseDTO computeStats() {
        List<Trip> todayTrips = tripRepository.findAll(); // Simplified for brevity
        return TripStatsResponseDTO.builder()
                .activeTrips(countActiveTrips())
                .todayBookedSeats(todayTrips.stream().mapToInt(t -> bookingService.countBookedSeats(t.getId())).sum())
                .build();
    }

    private String translateStatus(TripStatusEnum status) {
        if (status == null) return "N/A";
        switch (status) {
            case SCHEDULED: return "Đang chờ";
            case DEPARTED: return "Đang chạy";
            case COMPLETED: return "Đã hoàn thành";
            case CANCELLED: return "Đã hủy";
            default: return status.name();
        }
    }
}
