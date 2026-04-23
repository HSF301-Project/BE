package sp26.group.busticket.service;

import jakarta.validation.Valid;
import sp26.group.busticket.dto.trip.response.TripBookingResponseDTO;
import sp26.group.busticket.dto.trip.TripPageResponse;
import sp26.group.busticket.dto.trip.TripResponseDTO;
import sp26.group.busticket.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.dto.trip.response.TripDriverOptionDTO;
import sp26.group.busticket.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.dto.trip.response.TripStopEtaDTO;
import sp26.group.busticket.entity.Ticket;
import sp26.group.busticket.entity.Trip;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripService {
    // Client Side
    TripSearchResultDTO searchTrips(TripSearchRequestDTO request);
    BigDecimal getBasePriceByTripId(UUID tripId);
    List<TripStopEtaDTO> getTripStopEtas(UUID tripId);
    TripBookingResponseDTO getTripBookingData(UUID tripId);

    // Admin Side
    TripPageResponse getAdminDashboardData(String query, String status, int page, int size);
    TripResponseDTO getTripById(UUID id);
    TripRequestDTO getTripForEdit(UUID id);

    List<TripDriverOptionDTO> listAssignableDrivers();

    /** @return id tài khoản tài xế mới nếu vừa tạo (để hiển thị hướng dẫn mật khẩu tạm). */
    Optional<UUID> createTrip(@Valid TripRequestDTO request);

    Optional<UUID> updateTrip(UUID id, TripRequestDTO request);

    void deleteTrip(UUID id);

    // Operation Side
    void startTrip(UUID tripId);

    void finishTrip(UUID tripId);

    List<Trip> getStaffTrips(String email);
    Trip getTripEntityById(UUID tripId);
    List<Ticket> getTicketsByTripId(UUID tripId);

    // Business Logic for Admin Form
    Optional<UUID> findReturnRouteId(UUID forwardRouteId);
    boolean isDriverAvailable(UUID driverId, java.time.LocalDateTime start, java.time.LocalDateTime end, UUID excludeTripId);
    boolean isCoachAvailable(UUID coachId, LocalDateTime start, LocalDateTime end, UUID routeId, UUID excludeTripId);

    List<UUID> getAvailableDriverIds(java.time.LocalDateTime start, java.time.LocalDateTime end, UUID excludeTripId);

    List<UUID> getAvailableCoachIds(java.time.LocalDateTime start, java.time.LocalDateTime end, UUID routeId, UUID excludeTripId);
}