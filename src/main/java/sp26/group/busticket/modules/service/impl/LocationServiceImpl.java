package sp26.group.busticket.modules.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sp26.group.busticket.modules.entity.Location;
import sp26.group.busticket.modules.repository.LocationRepository;
import sp26.group.busticket.modules.service.LocationService;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;

    @Override
    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    @Override
    public long countTotalStations() {
        return locationRepository.count();
    }

    @Override
    public long countTotalCities() {
        return locationRepository.findAll().stream()
                .map(Location::getCity)
                .distinct()
                .count();
    }

    @Override
    public List<Location> getLocationsSortedByCity() {
        return locationRepository.findAll().stream()
                .sorted(Comparator.comparing(Location::getCity).thenComparing(Location::getName))
                .toList();
    }

    @Override
    public Optional<Location> findById(UUID id) {
        return locationRepository.findById(id);
    }

    @Override
    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public List<Location> getLocationsByType(String type) {
        return locationRepository.findByLocationType(type);
    }
}
