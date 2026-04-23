package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.service.LocationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public List<Location> getLocationsByType(String type) {
        return locationRepository.findByLocationType(type);
    }

    @Override
    public Location getLocationById(java.util.UUID id) {
        return locationRepository.findById(id).orElse(null);
    }

    @Override
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public void deleteLocation(java.util.UUID id) {
        locationRepository.deleteById(id);
    }

    @Override
    public long countLocations() {
        return locationRepository.count();
    }

    @Override
    public long countDistinctCities() {
        return locationRepository.findAll().stream()
                .map(Location::getCity).distinct().count();
    }

    @Override
    public Location findByAddress(String address) {
        return locationRepository.findByAddress(address).orElse(null);
    }
}
