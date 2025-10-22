package vroong.laas.delivery.core.domain.outbox;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.outbox.required.OutboxEventClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

  private final OutboxEventClient outboxEventClient;
  private final OutboxEventRepository outboxEventRepository;

  @Transactional
  public void publish(int size) {
    List<OutboxEvent> outboxEventEntities =
        outboxEventRepository.findByStatusOrderByCreatedAtDesc(
            OutboxEventStatus.REGISTERED,
            PageRequest.of(0, size));

    for (OutboxEvent entity: outboxEventEntities) {
      try {
        outboxEventClient.publish(entity);
        entity.markAsPublished();
        outboxEventRepository.save(entity);
      } catch (Exception e) {
        log.error("outbox 발행 싪패. token: {}", entity.getEventToken(), e);
      }
    }
  }
}
