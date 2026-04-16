package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Ticket;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBooking_Trip_Id(UUID tripId);
}
