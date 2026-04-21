package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;

import java.math.BigDecimal;
import java.util.UUID;

public interface TripService {
    TripSearchResultDTO searchTrips(TripSearchRequestDTO request);
    BigDecimal getBasePriceByTripId(UUID tripId);
}
