package sp26.group.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.entity.Ticket;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByBooking_Trip_Id(UUID tripId);

    List<Ticket> findByPassengerPhone(String passengerPhone);

    List<Ticket> findByBooking_Trip_Coach_Id(UUID coachId);

    boolean existsByBooking_Trip_Coach_Id(UUID coachId);

    List<Ticket> findBySeat_Coach_Id(UUID coachId);

    boolean existsBySeat_Coach_Id(UUID coachId);

    long countByBooking_Trip_Id(UUID tripId);

    long countByBooking_Trip_IdAndStatus(UUID tripId, String status);
}
