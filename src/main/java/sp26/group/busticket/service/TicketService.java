package sp26.group.busticket.service;

import java.util.UUID;

public interface TicketService {
    void checkInTicket(String ticketCode, UUID tripId);
    long countTicketsByTrip(UUID tripId);
    long countCheckedInTicketsByTrip(UUID tripId);
    void toggleCheckIn(UUID tripId, String ticketCode);
}
