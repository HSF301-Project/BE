package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.entity.Route;

import java.util.Optional;
import java.util.UUID;

public interface RouteRepository extends JpaRepository<Route, UUID> {
    Optional<Route> findByDepartureLocationAndArrivalLocation(Location departureLocation, Location arrivalLocation);
}
