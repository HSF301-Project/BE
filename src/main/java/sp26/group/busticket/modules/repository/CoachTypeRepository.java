package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.modules.entity.CoachType;
import java.util.UUID;

public interface CoachTypeRepository extends JpaRepository<CoachType, UUID> {
}
