package vroong.laas.order.data.entity.outbox;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

  List<OutboxEventEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

}
