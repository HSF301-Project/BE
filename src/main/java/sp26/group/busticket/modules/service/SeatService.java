package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.dto.booking.response.SeatDisplayDTO;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    List<SeatDisplayDTO> getSeatsByTripAndFloor(UUID tripId, Integer floor);
    List<SeatDisplayDTO> getSeatsByTripId(UUID tripId);
}
