package sp26.group.busticket.modules.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sp26.group.busticket.modules.entity.PaymentEntity;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Integer> {
    Optional<PaymentEntity> findByBookingId(Integer bookingId);
    Optional<PaymentEntity> findByTransactionId(String transactionId);
}
