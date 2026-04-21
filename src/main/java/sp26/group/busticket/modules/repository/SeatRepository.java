package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Seat;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {
    List<Seat> findByCoach_IdOrderBySeatNumberAsc(UUID coachId);
}
