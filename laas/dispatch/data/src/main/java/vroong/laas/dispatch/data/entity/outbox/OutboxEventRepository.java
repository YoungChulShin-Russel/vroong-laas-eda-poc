package vroong.laas.dispatch.data.entity.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventStatus;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

  List<OutboxEventEntity> findByStatusOrderByCreatedAtDesc(OutboxEventStatus status, Pageable pageable);

}
