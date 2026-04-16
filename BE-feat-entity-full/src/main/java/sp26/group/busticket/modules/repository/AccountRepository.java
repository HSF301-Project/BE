package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.AccountEntity;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Integer> {
    Optional<AccountEntity> findByEmail(String email);
    Optional<AccountEntity> findByPhone(String phone);
    boolean existsByEmail(String email);
}
