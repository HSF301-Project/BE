package sp26.group.busticket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sp26.group.busticket.entity.Coach;

import java.util.Optional;
import java.util.UUID;

public interface CoachRepository extends JpaRepository<Coach, UUID> {
    Optional<Coach> findByPlateNumber(String plateNumber);
    long countByCoachType_Id(UUID coachTypeId);
}
