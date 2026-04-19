package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Account> findAllByRoleOrderByCreatedAtDesc(String role);

    Optional<Account> findByIdAndRole(UUID id, String role);

    boolean existsByEmailAndIdNot(String email, UUID id);

    @Query("""
            SELECT a
            FROM Account a
            WHERE a.role = :role
              AND (
                   :keyword IS NULL OR :keyword = ''
                   OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(a.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
            """)
    Page<Account> searchStaffByKeyword(@Param("role") String role,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);
}
