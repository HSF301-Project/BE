package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.trip.request.TripSearchRequestDTO;
import sp26.group.busticket.modules.dto.trip.response.TripSearchResultDTO;

public interface TripService {
    TripSearchResultDTO searchTrips(TripSearchRequestDTO request);
}
