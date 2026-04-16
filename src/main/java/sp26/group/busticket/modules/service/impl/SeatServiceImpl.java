package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.common.exception.AppException;
import sp26.group.busticket.common.exception.ErrorCode;
import sp26.group.busticket.modules.dto.booking.response.SeatDisplayDTO;
import sp26.group.busticket.modules.entity.Seat;
import sp26.group.busticket.modules.entity.Ticket;
import sp26.group.busticket.modules.entity.Trip;
import sp26.group.busticket.modules.mapper.SeatMapper;
import sp26.group.busticket.modules.repository.SeatRepository;
import sp26.group.busticket.modules.repository.TicketRepository;
import sp26.group.busticket.modules.repository.TripRepository;
import sp26.group.busticket.modules.service.SeatService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatServiceImpl implements SeatService {

    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final TripRepository tripRepository;
    private final SeatMapper seatMapper;

    @Override
    public List<SeatDisplayDTO> getSeatsByTripAndFloor(UUID tripId, Integer floor) {
        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        List<Seat> allSeats = seatRepository.findByCoach_IdOrderBySeatNumberAsc(trip.getCoach().getId());
        
        List<Ticket> bookedTickets = ticketRepository.findByBooking_Trip_Id(tripId);
        Set<UUID> bookedSeatIds = bookedTickets.stream()
                .map(t -> t.getSeat().getId())
                .collect(Collectors.toSet());

        return allSeats.stream()
                .filter(s -> s.getFloor().equals(floor))
                .map(s -> {
                    SeatDisplayDTO dto = seatMapper.toSeatDisplayDTO(s);
                    dto.setStatus(bookedSeatIds.contains(s.getId()) ? "BOOKED" : "AVAILABLE");
                    dto.setAisleAfter(shouldAddAisle(s.getSeatNumber()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private boolean shouldAddAisle(String seatNumber) {
        // Simple logic for aisle: add aisle after every 2nd column in a 4-column layout
        // For example, if seatNumber is L02, L06, etc. (assuming 4 seats per row)
        // This is just a placeholder logic.
        try {
            int num = Integer.parseInt(seatNumber.substring(1));
            return num % 4 == 2;
        } catch (Exception e) {
            return false;
        }
    }
}
