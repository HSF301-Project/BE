package sp26.group.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.entity.CoachType;
import java.util.UUID;

public interface CoachTypeRepository extends JpaRepository<CoachType, UUID> {
    java.util.Optional<CoachType> findByName(String name);
}
