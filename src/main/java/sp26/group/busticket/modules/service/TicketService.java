package sp26.group.busticket.modules.service;

import java.util.UUID;

public interface TicketService {
    void checkInTicket(String ticketCode, UUID tripId);
}
