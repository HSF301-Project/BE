package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.SeatEntity;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<SeatEntity, Integer> {

    List<SeatEntity> findByCoachIdOrderBySeatNumber(Integer coachId);

    @Query("SELECT s FROM SeatEntity s WHERE s.coach.id = :coachId " +
           "AND s.id NOT IN " +
           "(SELECT t.seat.id FROM TicketEntity t " +
           "JOIN t.booking b " +
           "JOIN b.trip t2 " +
           "WHERE t2.id = :tripId AND b.status <> 'CANCELLED') " +
           "ORDER BY s.floor, s.seatNumber")
    List<SeatEntity> findAvailableSeatsByTripId(@Param("coachId") Integer coachId, @Param("tripId") Integer tripId);
}
