package sp26.group.busticket.modules.service;

import jakarta.validation.Valid;
import sp26.group.busticket.modules.dto.trip.TripPageResponse;
import sp26.group.busticket.modules.dto.trip.TripResponseDTO;
import sp26.group.busticket.modules.dto.trip.request.TripRequestDTO;
import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripDriverOptionDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;
import sp26.group.busticket.modules.dto.trip.response.TripStopEtaDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripService {
    // Client Side
    TripSearchResultDTO searchTrips(TripSearchRequestDTO request);
    BigDecimal getBasePriceByTripId(UUID tripId);
    List<TripStopEtaDTO> getTripStopEtas(UUID tripId);

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
}