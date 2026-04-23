package sp26.group.busticket.service;

import sp26.group.busticket.entity.Location;
import java.util.List;

public interface LocationService {
    List<Location> getAllLocations();
    List<Location> getLocationsByType(String type);
    Location getLocationById(java.util.UUID id);
    Location saveLocation(Location location);
    void deleteLocation(java.util.UUID id);
    long countLocations();
    long countDistinctCities();
    Location findByAddress(String address);
}
