package sp26.group.busticket.service;

import sp26.group.busticket.dto.booking.response.SeatDisplayDTO;
import sp26.group.busticket.entity.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatService {
    List<SeatDisplayDTO> getSeatsByTripAndFloor(UUID tripId, Integer floor);
    List<SeatDisplayDTO> getSeatsByTripId(UUID tripId);
    List<Seat> getSeatsByCoachId(UUID coachId);
}
