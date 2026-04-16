package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.CoachEntity;

@Repository
public interface CoachRepository extends JpaRepository<CoachEntity, Integer> {
}
