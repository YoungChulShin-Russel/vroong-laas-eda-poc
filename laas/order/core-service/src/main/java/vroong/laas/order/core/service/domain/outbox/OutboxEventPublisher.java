package vroong.laas.order.core.service.domain.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.enums.outbox.OutboxEventStatus;
import vroong.laas.order.core.service.domain.outbox.required.OutboxEventClient;
import vroong.laas.order.data.entity.outbox.OutboxEventEntity;
import vroong.laas.order.data.entity.outbox.OutboxEventRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

  private final OutboxEventClient outboxEventClient;
  private final OutboxEventRepository outboxEventRepository;

  @Transactional
  public void publish(int size) {
    List<OutboxEventEntity> outboxEventEntities =
        outboxEventRepository.findByStatusOrderByCreatedAtDesc(
            OutboxEventStatus.REGISTERED,
            PageRequest.of(0, size));

    for (OutboxEventEntity entity: outboxEventEntities) {
      OutboxEvent outboxEvent = OutboxEvent.fromEntity(entity);
      try {
        outboxEventClient.publish(outboxEvent);
        entity.markAsPublished();
        outboxEventRepository.save(entity);
      } catch (Exception e) {
        log.error("outbox 발행 싪패. token: {}", outboxEvent.getEventToken(), e);
      }
    }
  }
}
