package vroong.laas.delivery.core.domain.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

  List<OutboxEvent> findByStatusOrderByCreatedAtDesc(OutboxEventStatus status, Pageable pageable);

}
