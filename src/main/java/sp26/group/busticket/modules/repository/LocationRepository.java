package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.Location;

import java.util.Optional;
import java.util.UUID;

public interface LocationRepository extends JpaRepository<Location, UUID> {
    Optional<Location> findByName(String name);
}
