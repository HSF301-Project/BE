package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.RouteEntity;

import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<RouteEntity, Integer> {
    
    @Query("SELECT DISTINCT r FROM RouteEntity r " +
           "JOIN FETCH r.departureLocation dl " +
           "JOIN FETCH r.arrivalLocation al")
    List<RouteEntity> findAllWithLocations();
}
