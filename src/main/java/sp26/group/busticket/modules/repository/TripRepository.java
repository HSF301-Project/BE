package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Trip;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {
    List<Trip> findByRoute_DepartureLocation_NameAndRoute_ArrivalLocation_NameAndDepartureTimeBetween(
            String from, String to, LocalDateTime startOfDay, LocalDateTime endOfDay);
    
    boolean existsByCoach_Id(UUID coachId);
}
