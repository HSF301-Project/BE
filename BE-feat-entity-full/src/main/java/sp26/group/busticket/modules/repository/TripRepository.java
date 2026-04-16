package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.TripEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Integer> {
    
    @Query("SELECT t FROM TripEntity t JOIN FETCH t.route r " +
           "JOIN FETCH r.departureLocation dl " +
           "JOIN FETCH r.arrivalLocation al " +
           "JOIN FETCH t.coach WHERE t.departureTime BETWEEN :start AND :end ORDER BY t.departureTime")
    List<TripEntity> findByDepartureTimeBetween(
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT t FROM TripEntity t JOIN FETCH t.route r " +
           "JOIN FETCH r.departureLocation dl " +
           "JOIN FETCH r.arrivalLocation al " +
           "JOIN FETCH t.coach WHERE r.id = :routeId " +
           "AND t.departureTime BETWEEN :start AND :end ORDER BY t.departureTime")
    List<TripEntity> findByRouteIdAndDepartureTimeBetween(
        @Param("routeId") Integer routeId,
        @Param("start") LocalDateTime start, 
        @Param("end") LocalDateTime end
    );
    
    @Query("SELECT t FROM TripEntity t JOIN FETCH t.route r " +
           "JOIN FETCH r.departureLocation dl " +
           "JOIN FETCH r.arrivalLocation al " +
           "JOIN FETCH t.coach ORDER BY t.departureTime")
    List<TripEntity> findAllWithDetails();
}
