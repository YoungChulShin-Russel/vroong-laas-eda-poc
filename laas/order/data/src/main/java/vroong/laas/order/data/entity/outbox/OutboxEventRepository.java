package vroong.laas.order.data.entity.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

}
