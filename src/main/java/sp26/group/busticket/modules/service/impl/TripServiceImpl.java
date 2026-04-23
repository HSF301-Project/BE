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
import sp26.group.busticket.modules.dto.trip.TripResponseDTO;
import sp26.group.busticket.modules.dto.trip.TripStatsResponseDTO;
import sp26.group.busticket.modules.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripDriverOptionDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.modules.entity.Account;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.enumType.BookingStatusEnum;
import sp26.group.busticket.modules.enumType.StatusEnum;
import sp26.group.busticket.modules.enumType.TripStatusEnum;
import sp26.group.busticket.modules.mapper.TripMapper;
import sp26.group.busticket.modules.repository.*;
import sp26.group.busticket.modules.service.TripService;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedHashSet;
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
    private final TicketRepository ticketRepository;
    private final RouteRepository routeRepository;
    private final CoachRepository coachRepository;
    private final AccountRepository accountRepository;
    private final TripMapper tripMapper;
    private final PasswordEncoder passwordEncoder;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final Locale LOCALE_VN = new Locale("vi", "VN");

    // =====================================================================
    // CLIENT SIDE: SEARCH TRIPS
    // =====================================================================
    @Override
    public TripSearchResultDTO searchTrips(TripSearchRequestDTO request) {
        LocalDateTime now = LocalDateTime.now();

        // 1. Tìm chuyến đi (Departure Trips)
        List<Trip> departureTrips = performSearchEntities(request.getFrom(), request.getTo(), request.getDate(), request, now);
        List<sp26.group.busticket.modules.dto.trip.response.TripResponseDTO> departureTripDTOs = departureTrips.stream()
                .map(this::mapToClientResponseDTO)
                .filter(dto -> dto.getSeatsLeft() != null && dto.getSeatsLeft() > 0)
                .collect(Collectors.toList());

        String dateLabel = "";
        String dateValue = "";
        if (request.getDate() != null && !request.getDate().isBlank()) {
            LocalDate date = LocalDate.parse(request.getDate());
            dateLabel = date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy"));
            dateValue = date.toString();
        } else {
            dateLabel = "Tất cả ngày";
        }

        TripSearchResultDTO.TripSearchResultDTOBuilder builder = TripSearchResultDTO.builder()
                .fromCity(request.getFrom())
                .toCity(request.getTo())
                .date(dateValue)
                .dateLabel(dateLabel)
                .trips(departureTripDTOs)
                .totalCount((long) departureTripDTOs.size())
                .roundTrip(request.isRoundTrip());

        // 2. Tìm chuyến về (Return Trips) nếu là khứ hồi
        if (request.isRoundTrip() && StringUtils.hasText(request.getReturnDate())) {
            List<Trip> returnTrips = performSearchEntities(request.getTo(), request.getFrom(), request.getReturnDate(), request, now);

            // Áp dụng ràng buộc: (departureTime về > arrivalTime đi)
            if (!departureTrips.isEmpty() && !returnTrips.isEmpty()) {
                LocalDateTime minArrivalOfDeparture = departureTrips.stream()
                        .map(Trip::getArrivalTime)
                        .min(LocalDateTime::compareTo)
                        .orElse(now);

                returnTrips = returnTrips.stream()
                        .filter(rt -> rt.getDepartureTime().isAfter(minArrivalOfDeparture))
                        .collect(Collectors.toList());
            }

            List<sp26.group.busticket.modules.dto.trip.response.TripResponseDTO> returnTripDTOs = returnTrips.stream()
                    .map(this::mapToClientResponseDTO)
                    .filter(dto -> dto.getSeatsLeft() != null && dto.getSeatsLeft() > 0)
                    .collect(Collectors.toList());

            LocalDate returnDate = LocalDate.parse(request.getReturnDate());
            builder.returnTrips(returnTripDTOs)
                   .totalReturnCount((long) returnTripDTOs.size())
                   .returnDate(request.getReturnDate())
                   .returnDateLabel(returnDate.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy")));
        }

        return builder.build();
    }

    private List<Trip> performSearchEntities(
            String from, String to, String dateStr, TripSearchRequestDTO request, LocalDateTime now) {

        List<Trip> trips;
        boolean hasDate = StringUtils.hasText(dateStr);

        if (hasDate) {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDateTime searchStart = date.equals(now.toLocalDate()) ? now : date.atStartOfDay();
            LocalDateTime searchEnd = date.atTime(LocalTime.MAX);
            trips = tripRepository.findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
                    from, to, searchStart, searchEnd);
        } else {
            trips = tripRepository.findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeAfter(
                    from, to, now);
        }

        return trips.stream()
                .filter(t -> request.getBusType() == null || request.getBusType().isEmpty() ||
                        t.getCoach().getCoachType().getName().equalsIgnoreCase(request.getBusType()))
                .filter(t -> request.getMaxPrice() == null ||
                        t.getPriceBase().compareTo(BigDecimal.valueOf(request.getMaxPrice())) <= 0)
                .collect(Collectors.toList());
    }

    // =====================================================================
    // ADMIN SIDE: DASHBOARD & CRUD
    // =====================================================================
    @Override
    public TripPageResponse getAdminDashboardData(String query, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("departureTime").descending());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonTime = now.plusHours(2);
        Page<Trip> tripPage = tripRepository.findAllBySearchAndStatus(query != null ? query : "", status, now, soonTime, pageable);

        List<TripResponseDTO> dtos = tripPage.getContent().stream()
                .map(this::mapToAdminResponseDTO)
                .collect(Collectors.toList());

        return TripPageResponse.builder()
                .trips(dtos)
                .stats(calculateTodayStats())
                .currentPage(page)
                .totalPages(tripPage.getTotalPages())
                .totalCount(tripPage.getTotalElements())
                .displayedCount(dtos.size())
                .hasNext(tripPage.hasNext())
                .build();
    }

    @Override
    public TripResponseDTO getTripById(UUID id) {
        return tripRepository.findById(id)
                .map(this::mapToAdminResponseDTO)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public TripRequestDTO getTripForEdit(UUID id) {
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        return TripRequestDTO.builder()
                .id(trip.getId())
                .routeId(trip.getRoute().getId())
                .coachId(trip.getCoach().getId())
                .driverId(trip.getDriver() != null ? trip.getDriver().getId() : null)
                .secondDriverId(trip.getSecondDriver() != null ? trip.getSecondDriver().getId() : null)
                .assistantId(trip.getAssistant() != null ? trip.getAssistant().getId() : null)
                .departureTime(trip.getDepartureTime())
                .arrivalTime(trip.getArrivalTime())
                .priceBase(trip.getPriceBase())
                .contactPhoneNumber(trip.getContactPhoneNumber())
                .status(trip.getTripStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TripDriverOptionDTO> listAssignableDrivers() {
        return accountRepository.findByRoleAndStatusOrderByFullNameAsc("DRIVER", StatusEnum.ACTIVE).stream()
                .map(a -> TripDriverOptionDTO.builder()
                        .id(a.getId())
                        .fullName(a.getFullName())
                        .phone(a.getPhone())
                        .licenseNumber(a.getDriverLicenseNumber())
                        .readinessLabel("Sẵn sàng")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UUID> createTrip(TripRequestDTO req) {
        validateTripBusinessRules(req, true);
        
        Trip trip = Trip.builder()
                .route(routeRepository.findById(req.getRouteId()).orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND)))
                .coach(coachRepository.findById(req.getCoachId()).orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND)))
                .driver(accountRepository.findById(req.getDriverId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)))
                .secondDriver(req.getSecondDriverId() != null
                        ? accountRepository.findById(req.getSecondDriverId()).orElse(null)
                        : null)
                .assistant(req.getAssistantId() != null
                        ? accountRepository.findById(req.getAssistantId()).orElse(null)
                        : null)
                .departureTime(req.getDepartureTime())
                .arrivalTime(req.getArrivalTime())
                .priceBase(req.getPriceBase())
                .contactPhoneNumber(req.getContactPhoneNumber())
                .tripStatus(req.getStatus())
                .build();
        tripRepository.save(trip);

        // Handle Roundtrip
        if (req.isRoundTrip() && req.getReturnRouteId() != null && req.getReturnDepartureTime() != null) {
            TripRequestDTO returnReq = TripRequestDTO.builder()
                    .routeId(req.getReturnRouteId())
                    .coachId(req.getCoachId())
                    .driverId(req.getReturnDriverId())
                .secondDriverId(req.getReturnSecondDriverId())
                    .assistantId(req.getAssistantId()) // Dùng chung phụ xe cho cả 2 lượt
                    .departureTime(req.getReturnDepartureTime())
                    .arrivalTime(req.getReturnArrivalTime())
                    .priceBase(req.getPriceRoundTrip() != null ? req.getPriceRoundTrip() : req.getPriceBase())
                    .contactPhoneNumber(req.getContactPhoneNumber())
                    .status(req.getStatus())
                    .build();
            
            // Validate return trip separately
            validateTripBusinessRules(returnReq, true);

            Trip returnTrip = Trip.builder()
                    .route(routeRepository.findById(returnReq.getRouteId()).orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND)))
                    .coach(coachRepository.findById(returnReq.getCoachId()).orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND)))
                    .driver(accountRepository.findById(returnReq.getDriverId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)))
                    .secondDriver(returnReq.getSecondDriverId() != null
                            ? accountRepository.findById(returnReq.getSecondDriverId()).orElse(null)
                            : null)
                    .assistant(returnReq.getAssistantId() != null
                            ? accountRepository.findById(returnReq.getAssistantId()).orElse(null)
                            : null)
                    .departureTime(returnReq.getDepartureTime())
                    .arrivalTime(returnReq.getArrivalTime())
                    .priceBase(returnReq.getPriceBase())
                    .contactPhoneNumber(returnReq.getContactPhoneNumber())
                    .tripStatus(returnReq.getStatus())
                    .build();
            tripRepository.save(returnTrip);
        }

        return Optional.empty();
    }

    @Override
    public Optional<UUID> findReturnRouteId(UUID forwardRouteId) {
        var forward = routeRepository.findById(forwardRouteId)
                .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND));
        
        return routeRepository.findAll().stream()
                .filter(r -> r.getDepartureLocation().getId().equals(forward.getArrivalLocation().getId()) &&
                            r.getArrivalLocation().getId().equals(forward.getDepartureLocation().getId()))
                .map(sp26.group.busticket.infrastructure.persistence.BaseEntity::getId)
                .findFirst();
    }

    @Override
    public boolean isDriverAvailable(UUID driverId, LocalDateTime start, LocalDateTime end, UUID excludeTripId) {
        try {
            checkDriverConflict(driverId, start, end, excludeTripId, "Tài xế");
            return true;
        } catch (AppException e) {
            return false;
        }
    }

    @Override
    public boolean isCoachAvailable(UUID coachId, LocalDateTime start, LocalDateTime end, UUID routeId, UUID excludeTripId) {
        List<Trip> coachTrips = tripRepository.findAllTripsByCoach(coachId);
        
        // Lấy thông tin tuyến đường mới để check vị trí
        var newRoute = routeId != null ? routeRepository.findById(routeId).orElse(null) : null;
        String newStartLocation = newRoute != null ? newRoute.getDepartureLocation().getName() : null;
        String newEndLocation = newRoute != null ? newRoute.getArrivalLocation().getName() : null;

        for (Trip existing : coachTrips) {
            if (excludeTripId != null && existing.getId().equals(excludeTripId)) continue;
            
            // 1. Check xung đột thời gian (Overlap) với 60 phút nghỉ ngơi
            if (start.isBefore(existing.getArrivalTime().plusMinutes(60)) && 
                end.isAfter(existing.getDepartureTime().minusMinutes(60))) {
                return false;
            }

            // 2. Check xung đột vị trí nếu có routeId
            if (newRoute != null) {
                // Nếu chuyến mới sau chuyến cũ
                if (start.isAfter(existing.getArrivalTime())) {
                    String lastStop = existing.getRoute().getArrivalLocation().getName();
                    if (!lastStop.equalsIgnoreCase(newStartLocation)) {
                        return false;
                    }
                }
                // Nếu chuyến mới trước chuyến cũ
                if (end.isBefore(existing.getDepartureTime())) {
                    String nextStart = existing.getRoute().getDepartureLocation().getName();
                    if (!newEndLocation.equalsIgnoreCase(nextStart)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<UUID> getAvailableDriverIds(LocalDateTime start, LocalDateTime end, UUID excludeTripId) {
        return accountRepository.findByRoleAndStatusOrderByFullNameAsc("DRIVER", StatusEnum.ACTIVE).stream()
                .filter(a -> isDriverAvailable(a.getId(), start, end, excludeTripId))
                .map(sp26.group.busticket.infrastructure.persistence.BaseEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<UUID> getAvailableCoachIds(LocalDateTime start, LocalDateTime end, UUID routeId, UUID excludeTripId) {
        return coachRepository.findAll().stream()
                .filter(c -> isCoachAvailable(c.getId(), start, end, routeId, excludeTripId))
                .map(sp26.group.busticket.infrastructure.persistence.BaseEntity::getId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<UUID> updateTrip(UUID id, TripRequestDTO req) {
        validateTripBusinessRules(req, false);
        Trip trip = tripRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        trip.setRoute(routeRepository.findById(req.getRouteId()).orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND)));
        trip.setCoach(coachRepository.findById(req.getCoachId()).orElseThrow(() -> new AppException(ErrorCode.COACH_NOT_FOUND)));
        trip.setDriver(accountRepository.findById(req.getDriverId()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
        trip.setSecondDriver(req.getSecondDriverId() != null
                ? accountRepository.findById(req.getSecondDriverId()).orElse(null)
                : null);
        trip.setAssistant(req.getAssistantId() != null
                ? accountRepository.findById(req.getAssistantId()).orElse(null)
                : null);
        trip.setDepartureTime(req.getDepartureTime());
        trip.setArrivalTime(req.getArrivalTime());
        trip.setPriceBase(req.getPriceBase());
        trip.setContactPhoneNumber(req.getContactPhoneNumber());
        trip.setTripStatus(req.getStatus());

        tripRepository.save(trip);
        return Optional.empty();
    }

    @Override
    @Transactional
    public void deleteTrip(UUID id) {
        if (!tripRepository.existsById(id)) throw new AppException(ErrorCode.TRIP_NOT_FOUND);
        tripRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void startTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        if (trip.getTripStatus() != TripStatusEnum.SCHEDULED) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể bắt đầu chuyến đi đang ở trạng thái SCHEDULED.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(trip.getDepartureTime())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            throw new AppException(ErrorCode.INVALID_INPUT, 
                "Không thể bắt đầu chuyến xe sớm hơn giờ dự kiến. Vui lòng đợi đến " + trip.getDepartureTime().format(formatter) + ".");
        }

        trip.setActualDepartureTime(now);
        trip.setTripStatus(TripStatusEnum.DEPARTED);
        
        // Cập nhật trạng thái xe sang đang làm việc
        if (trip.getCoach() != null) {
            trip.getCoach().setStatus(sp26.group.busticket.modules.enumType.CoachStatusEnum.WORKING);
            coachRepository.save(trip.getCoach());
        }
        
        tripRepository.save(trip);
    }

    @Override
    @Transactional
    public void finishTrip(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));

        if (trip.getTripStatus() != TripStatusEnum.DEPARTED) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Chỉ có thể kết thúc chuyến đi đang ở trạng thái DEPARTED.");
        }

        trip.setActualArrivalTime(LocalDateTime.now());
        trip.setTripStatus(TripStatusEnum.COMPLETED);
        
        // Giải phóng xe về trạng thái sẵn sàng
        if (trip.getCoach() != null) {
            trip.getCoach().setStatus(sp26.group.busticket.modules.enumType.CoachStatusEnum.AVAILABLE);
            coachRepository.save(trip.getCoach());
        }
        
        tripRepository.save(trip);
    }

    @Override
    public BigDecimal getBasePriceByTripId(UUID tripId) {
        return tripRepository.findById(tripId)
                .map(Trip::getPriceBase)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
    }

    @Override
    public List<TripStopEtaDTO> getTripStopEtas(UUID tripId) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.TRIP_NOT_FOUND));
        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH:mm");
        return buildRouteTimeline(trip, timeOnly);
    }

    // =====================================================================
    // PRIVATE HELPERS
    // =====================================================================

    private void validateTripBusinessRules(TripRequestDTO req, boolean enforceFutureDeparture) {
        // Calculate arrivalTime if duration is provided
        if (req.getArrivalTime() == null && req.getDepartureTime() != null && req.getTravelTimeHours() != null) {
            int totalMins = req.getTravelTimeHours() * 60 + (req.getTravelTimeMinutes() != null ? req.getTravelTimeMinutes() : 0);
            req.setArrivalTime(req.getDepartureTime().plusMinutes(totalMins));
        }

        if (req.getDepartureTime() == null || req.getArrivalTime() == null) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thiếu thời gian khởi hành hoặc thời gian đến.");
        }
        if (!req.getArrivalTime().isAfter(req.getDepartureTime())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thời gian đến dự kiến phải sau thời gian khởi hành.");
        }
        if (enforceFutureDeparture && !req.getDepartureTime().isAfter(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVALID_INPUT, "Thời gian khởi hành phải ở tương lai.");
        }

        // Overlapping trip validation using in-memory check (avoids adding new repository method)
        LocalDateTime dep = req.getDepartureTime();
        LocalDateTime arr = req.getArrivalTime();
        UUID excludeId = req.getId();

        if (req.getCoachId() != null) {
            List<Trip> coachTrips = tripRepository.findAllTripsByCoach(req.getCoachId());
            
            for (Trip existing : coachTrips) {
                // Nếu là update, bỏ qua chính nó
                if (excludeId != null && existing.getId().equals(excludeId)) continue;

                // 1. Check xung đột thời gian (Overlap)
                boolean isOverlapping = dep.isBefore(existing.getArrivalTime()) && 
                                       arr.isAfter(existing.getDepartureTime());
                
                if (isOverlapping) {
                    throw new AppException(ErrorCode.INVALID_INPUT, "Xe đang trong một lịch trình khác!");
                }

                // 2. Check thời gian nghỉ (Buffer Time - ít nhất 60 phút)
                // Nếu chuyến mới khởi hành SAU khi chuyến cũ kết thúc
                if (dep.isAfter(existing.getArrivalTime())) {
                    LocalDateTime earliestPossible = existing.getArrivalTime().plusMinutes(60);
                    if (dep.isBefore(earliestPossible)) {
                        throw new AppException(ErrorCode.INVALID_INPUT, "Xe cần ít nhất 60 phút nghỉ ngơi giữa 2 chuyến.");
                    }

                    // 3. Check xung đột vị trí
                    // Điểm xuất phát của chuyến mới phải là điểm đến của chuyến cũ
                    String lastStop = existing.getRoute().getArrivalLocation().getName();
                    String nextStart = routeRepository.findById(req.getRouteId())
                            .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND))
                            .getDepartureLocation().getName();
                    
                    if (!lastStop.equalsIgnoreCase(nextStart)) {
                        throw new AppException(ErrorCode.INVALID_INPUT, 
                            "Xung đột vị trí: Xe đang ở " + lastStop + ", không thể khởi hành từ " + nextStart);
                    }
                }
                
                // 4. Check nếu chuyến mới kết thúc TRƯỚC khi một chuyến đã có bắt đầu
                if (arr.isBefore(existing.getDepartureTime())) {
                    LocalDateTime latestPossibleEnd = existing.getDepartureTime().minusMinutes(60);
                    if (arr.isAfter(latestPossibleEnd)) {
                        throw new AppException(ErrorCode.INVALID_INPUT, "Xe cần ít nhất 60 phút nghỉ ngơi trước chuyến tiếp theo.");
                    }
                    
                    // Điểm đến của chuyến mới phải là điểm xuất phát của chuyến sau
                    String currentArrival = routeRepository.findById(req.getRouteId())
                            .orElseThrow(() -> new AppException(ErrorCode.ROUTE_NOT_FOUND))
                            .getArrivalLocation().getName();
                    String nextDeparture = existing.getRoute().getDepartureLocation().getName();
                    
                    if (!currentArrival.equalsIgnoreCase(nextDeparture)) {
                        throw new AppException(ErrorCode.INVALID_INPUT,
                            "Xung đột vị trí: Chuyến tiếp theo khởi hành từ " + nextDeparture + ", xe không thể kết thúc tại " + currentArrival);
                    }
                }
            }
        }

        if (req.getDriverId() != null) {
            checkDriverConflict(req.getDriverId(), dep, arr, excludeId, "Tài xế chính");
        }

        // 5. Kiểm tra Tài xế 2 (Bắt buộc cho chuyến > 4 tiếng)
        long durationMinutes = java.time.Duration.between(dep, arr).toMinutes();
        if (durationMinutes > 240) {
            if (req.getSecondDriverId() == null) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Chuyến đi kéo dài hơn 4 tiếng (" + durationMinutes + " phút), bắt buộc phải có tài xế thứ hai.");
            }
            if (req.getSecondDriverId().equals(req.getDriverId())) {
                throw new AppException(ErrorCode.INVALID_INPUT, "Tài xế thứ hai không được trùng với tài xế chính.");
            }
            checkDriverConflict(req.getSecondDriverId(), dep, arr, excludeId, "Tài xế thứ hai");
        }
    }

    private void checkDriverConflict(UUID driverId, LocalDateTime dep, LocalDateTime arr, UUID excludeId, String roleLabel) {
        List<Trip> driverTrips = tripRepository.findAll().stream()
            .filter(t -> (t.getDriver() != null && t.getDriver().getId().equals(driverId)) || 
                         (t.getSecondDriver() != null && t.getSecondDriver().getId().equals(driverId)))
            .filter(t -> !t.getTripStatus().equals(TripStatusEnum.COMPLETED))
            .filter(t -> excludeId == null || !t.getId().equals(excludeId))
            .filter(t -> t.getDepartureTime().isBefore(arr) && t.getArrivalTime().isAfter(dep))
            .collect(Collectors.toList());
        if (!driverTrips.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_INPUT, roleLabel + " đã được xếp vào một chuyến đi khác trong khoảng thời gian này.");
        }
    }



    private static void assertDriverAssignable(Account driver) {
        if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
            throw new AppException(ErrorCode.DRIVER_NOT_ASSIGNABLE);
        }
        if (driver.getStatus() != StatusEnum.ACTIVE) {
            throw new AppException(ErrorCode.DRIVER_NOT_ASSIGNABLE);
        }
    }

    private static String trimToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String normalizeStops(String stopPointsRaw) {
        if (!StringUtils.hasText(stopPointsRaw)) {
            return null;
        }
        LinkedHashSet<String> uniqueStops = Arrays.stream(stopPointsRaw.split("\\r?\\n"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (uniqueStops.isEmpty()) {
            return null;
        }
        return String.join("||", uniqueStops);
    }

    private static String formatStopsForTextarea(String intermediateStops) {
        if (!StringUtils.hasText(intermediateStops)) {
            return null;
        }
        return Arrays.stream(intermediateStops.split("\\|\\|"))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.joining("\n"));
    }

    private static String getFirstStop(String normalizedStops) {
        if (!StringUtils.hasText(normalizedStops)) {
            return null;
        }
        return normalizedStops.split("\\|\\|")[0].trim();
    }

    private sp26.group.busticket.modules.dto.trip.response.TripResponseDTO mapToClientResponseDTO(Trip trip) {
        sp26.group.busticket.modules.dto.trip.response.TripResponseDTO dto = tripMapper.toTripResponseDTO(trip);
        int booked = getBookedSeats(trip.getId());
        int total = trip.getCoach().getTotalSeats() != null ? trip.getCoach().getTotalSeats() : 0;
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM, yyyy", LOCALE_VN);

        dto.setSeatsLeft(total - booked);
        dto.setImageUrl("https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=400");
        dto.setPriceFormatted(NumberFormat.getCurrencyInstance(LOCALE_VN).format(trip.getPriceBase()));
        dto.setDepartureTime(trip.getDepartureTime().format(TIME_FORMATTER));
        dto.setDepartureDateLabel(trip.getDepartureTime().format(dateFmt));
        dto.setArrivalTime(trip.getArrivalTime().format(TIME_FORMATTER));
        dto.setArrivalDateLabel(trip.getArrivalTime().format(dateFmt));
        dto.setDuration(calculateDuration(trip.getDepartureTime(), trip.getArrivalTime()));
        dto.setDepartureAmPm(trip.getDepartureTime().getHour() < 12 ? "SA" : "CH");
        return dto;
    }

    private TripResponseDTO mapToAdminResponseDTO(Trip trip) {
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");
        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH:mm");

        int booked = getBookedSeats(trip.getId());

        int total = (trip.getCoach() != null && trip.getCoach().getTotalSeats() != null)
                ? trip.getCoach().getTotalSeats()
                : 0;
        int fillPercent = (total > 0) ? (int) ((double) booked / total * 100) : 0;

        List<TripStopEtaDTO> timeline = buildRouteTimeline(trip, timeOnly);
        String nextStop = computeNextStopLabel(trip, timeline);
        
        Integer minutesUntilDeparture = null;
        if (trip.getTripStatus() == TripStatusEnum.SCHEDULED) {
            long mins = java.time.Duration.between(LocalDateTime.now(), trip.getDepartureTime()).toMinutes();
            minutesUntilDeparture = (int) mins;
        }

        return TripResponseDTO.builder()
                .id(trip.getId())
                .tripCode("TRP-" + trip.getId().toString().substring(0, 8).toUpperCase())
                .fromStation(trip.getRoute().getDepartureLocation().getName())
                .toStation(trip.getRoute().getArrivalLocation().getName())
                .fromCity(trip.getRoute().getDepartureLocation().getCity())
                .toCity(trip.getRoute().getArrivalLocation().getCity())
                .busType(trip.getCoach().getCoachType().getName())
                .busTypeLabel("Hạng " + trip.getCoach().getCoachType().getName())
                .departureTime(trip.getDepartureTime().format(timeFmt))
                .departureAmPm(trip.getDepartureTime().getHour() < 12 ? "SA" : "CH")
                .bookedSeats(booked)
                .totalSeats(total)
                .fillPercent(fillPercent)
                .status(trip.getTripStatus().name())
                .statusLabel(translateStatus(trip.getTripStatus()))
                .driverName(trip.getDriver() != null ? trip.getDriver().getFullName() : "Chưa phân công")
                .driverPhone(trip.getDriver() != null ? trip.getDriver().getPhone() : "N/A")
                .assistantName(trip.getAssistant() != null ? trip.getAssistant().getFullName() : "Chưa phân công")
                .assistantPhone(trip.getAssistant() != null ? trip.getAssistant().getPhone() : "N/A")
                .coachPlate(trip.getCoach() != null ? trip.getCoach().getPlateNumber() : "N/A")
                .departureDateTime(trip.getDepartureTime().format(dateTimeFmt))
                .arrivalDateTime(trip.getArrivalTime().format(dateTimeFmt))
                .arrivalTime(trip.getArrivalTime().format(timeFmt))
                .routeTimeline(timeline)
                .nextStopLabel(nextStop)
                .minutesUntilDeparture(minutesUntilDeparture)
                .formattedDepartureCountdown(minutesUntilDeparture != null ? formatDuration(minutesUntilDeparture) : null)
                .build();
    }

    private List<TripStopEtaDTO> buildRouteTimeline(Trip trip, DateTimeFormatter timeOnly) {
        // Sử dụng giờ xuất bến thực tế nếu đã chạy, ngược lại dùng giờ dự kiến
        LocalDateTime baseTime = (trip.getActualDepartureTime() != null) 
                ? trip.getActualDepartureTime() 
                : trip.getDepartureTime();

        List<StopWithKm> stops = new ArrayList<>();
        stops.add(new StopWithKm(trip.getRoute().getDepartureLocation().getName(), 0f, "START", null, trip.getRoute().getDepartureLocation().getId()));

        // Iterate structured RouteStop list (sorted by stopOrder)
        if (trip.getRoute().getStops() != null) {
            for (sp26.group.busticket.modules.entity.RouteStop rs : trip.getRoute().getStops()) {
                String stopType = rs.getStopType() != null ? rs.getStopType().name() : "BOTH";
                stops.add(new StopWithKm(
                        rs.getLocation().getName(),
                        rs.getDistanceFromStart(),
                        "INTERMEDIATE",
                        new StopMeta(stopType, rs.getOffsetMinutes()),
                        rs.getLocation().getId()));
            }
        }

        Float totalKm = trip.getRoute().getDistance();
        if (totalKm == null || totalKm <= 0) totalKm = 1f;
        stops.add(new StopWithKm(trip.getRoute().getArrivalLocation().getName(), totalKm, "END", null, trip.getRoute().getArrivalLocation().getId()));

        long totalMinutes = java.time.Duration.between(trip.getDepartureTime(), trip.getArrivalTime()).toMinutes();
        if (totalMinutes <= 0 && trip.getRoute().getDuration() != null) totalMinutes = trip.getRoute().getDuration();
        if (totalMinutes <= 0) totalMinutes = 60;

        boolean canUseKm = stops.stream().filter(s -> s.km != null).count() >= 2;
        int segmentsFallback = Math.max(stops.size() - 1, 1);

        List<TripStopEtaDTO> result = new ArrayList<>();
        // Sort only for km-based ratio calculations when km exists, but keep original order for display.
        List<StopWithKm> withKmSorted = stops.stream().filter(s -> s.km != null)
                .sorted(Comparator.comparing(s -> s.km)).toList();

        for (int i = 0; i < stops.size(); i++) {
            StopWithKm s = stops.get(i);
            long offsetMinutes;
            if (s.meta != null && s.meta.offsetMinutes != null) {
                offsetMinutes = s.meta.offsetMinutes;
            } else if (canUseKm && s.km != null) {
                offsetMinutes = Math.round(totalMinutes * (s.km / totalKm));
            } else {
                offsetMinutes = Math.round((double) i * totalMinutes / segmentsFallback);
            }

            LocalDateTime eta = baseTime.plusMinutes(offsetMinutes);
            String stopType = s.type;
            String pointType = (s.meta != null && s.meta.pointType != null && !s.meta.pointType.isBlank())
                    ? s.meta.pointType
                    : "BOTH";
            String pointTypeLabel = switch (pointType.toUpperCase()) {
                case "PICKUP" -> "Chỉ đón";
                case "DROPOFF" -> "Chỉ trả";
                default -> "Đón & trả";
            };
            result.add(TripStopEtaDTO.builder()
                    .stopId(s.locationId)
                    .stopName(s.name)
                    .etaTime(eta.format(timeOnly))
                    .stopType(stopType)
                    .pointType(pointType.toUpperCase())
                    .pointTypeLabel(pointTypeLabel)
                    .offsetMinutes((int) offsetMinutes)
                    .formattedOffset(formatDuration((int) offsetMinutes))
                    .build());
        }
        return result;
    }

    private String computeNextStopLabel(Trip trip, List<TripStopEtaDTO> timeline) {
        if (timeline == null || timeline.isEmpty()) return "N/A";
        if (trip.getTripStatus() != TripStatusEnum.DEPARTED) {
            return trip.getTripStatus() == TripStatusEnum.SCHEDULED ? "Chưa chạy" : "Hoàn thành";
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeOnly = DateTimeFormatter.ofPattern("HH:mm");
        for (TripStopEtaDTO s : timeline) {
            try {
                // etaTime is HH:mm of the same day; use departure date.
                LocalDateTime eta = trip.getDepartureTime().toLocalDate().atTime(LocalTime.parse(s.getEtaTime(), timeOnly));
                if (eta.isAfter(now)) {
                    return s.getStopName() + " (" + s.getEtaTime() + ")";
                }
            } catch (Exception ignored) {
                // ignore parse issues
            }
        }
        return timeline.get(timeline.size() - 1).getStopName();
    }

    private static class StopWithKm {
        final String name;
        final Float km;
        final String type;
        final StopMeta meta;
        final UUID locationId;

        private StopWithKm(String name, Float km, String type, StopMeta meta, UUID locationId) {
            this.name = name;
            this.km = km;
            this.type = type;
            this.meta = meta;
            this.locationId = locationId;
        }
    }

    private static class StopMeta {
        final String pointType;
        final Integer offsetMinutes;

        private StopMeta(String pointType, Integer offsetMinutes) {
            this.pointType = pointType;
            this.offsetMinutes = offsetMinutes;
        }
    }

    private int getBookedSeats(UUID tripId) {
        return (int) ticketRepository.findByBooking_Trip_Id(tripId).stream()
                .filter(t -> t.getBooking().getStatus() == BookingStatusEnum.PENDING ||
                        t.getBooking().getStatus() == BookingStatusEnum.CONFIRMED)
                .count();
    }

    private String translateStatus(TripStatusEnum status) {
        return switch (status) {
            case SCHEDULED -> "Đã lên lịch";
            case DEPARTED -> "Đang chạy";
            case COMPLETED -> "Hoàn thành";
        };
    }

    private TripStatsResponseDTO calculateTodayStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        List<Trip> todayTrips = tripRepository.findAllTripsToday(start, end);

        int unassignedDrivers = (int) todayTrips.stream().filter(t -> t.getDriver() == null).count();

        int todayBookedSeats = todayTrips.stream().mapToInt(t -> getBookedSeats(t.getId())).sum();
        int todayTotalSeats = todayTrips.stream()
                .mapToInt(t -> t.getCoach() != null && t.getCoach().getTotalSeats() != null
                        ? t.getCoach().getTotalSeats()
                        : 0)
                .sum();
        
        double avgFillRate = todayTotalSeats > 0 ? (double) todayBookedSeats / todayTotalSeats * 100 : 0.0;
        avgFillRate = Math.round(avgFillRate * 10.0) / 10.0;

        return TripStatsResponseDTO.builder()
                .todayTrips((long) todayTrips.size())
                .totalSeats(todayTotalSeats)
                .avgFillRate(avgFillRate)
                .alertCount(unassignedDrivers)
                .build();
    }

    private String calculateDuration(LocalDateTime start, LocalDateTime end) {
        java.time.Duration duration = java.time.Duration.between(start, end);
        return formatDuration((int) duration.toMinutes());
    }

    private String formatDuration(int totalMinutes) {
        if (totalMinutes <= 0) return "0 phút";
        int days = totalMinutes / (24 * 60);
        int hours = (totalMinutes % (24 * 60)) / 60;
        int mins = totalMinutes % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" ngày ");
        if (hours > 0) sb.append(hours).append(" giờ ");
        if (mins > 0 || sb.length() == 0) sb.append(mins).append(" phút");
        return sb.toString().trim();
    }
}
