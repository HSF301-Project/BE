package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Ticket;

import java.util.List;
import java.util.UUID;

public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    List<Ticket> findByBooking_Trip_Id(UUID tripId);
    List<Ticket> findByPassengerPhone(String passengerPhone);
    List<Ticket> findByBooking_Trip_Coach_Id(UUID coachId);
    boolean existsByBooking_Trip_Coach_Id(UUID coachId);
    
    @org.springframework.data.jpa.repository.Query("SELECT t FROM Ticket t WHERE t.seat.coach.id = :coachId")
    List<Ticket> findAllByCoachId(@org.springframework.data.repository.query.Param("coachId") UUID coachId);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(t) > 0 FROM Ticket t WHERE t.seat.coach.id = :coachId")
    boolean existsByCoachIdDirect(@org.springframework.data.repository.query.Param("coachId") UUID coachId);
}
