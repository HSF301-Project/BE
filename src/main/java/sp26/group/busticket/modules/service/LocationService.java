package sp26.group.busticket.modules.service;

import sp26.group.busticket.modules.entity.Location;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LocationService {
    List<Location> getAllLocations();
    long countTotalStations();
    long countTotalCities();
    List<Location> getLocationsSortedByCity();
    Optional<Location> findById(UUID id);
    Location saveLocation(Location location);
}
