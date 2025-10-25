package vroong.laas.order.core.service.domain.outbox;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.enums.outbox.OutboxEventStatus;
import vroong.laas.order.core.enums.outbox.OutboxEventType;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.data.entity.outbox.OutboxEventEntity;
import vroong.laas.order.data.entity.outbox.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxEventAppender {

  private final OutboxEventPayloadGenerator payloadGenerator;
  private final OutboxEventRepository outboxEventRepository;

  @Transactional
  public void appendOrderCreated(Order order) {
    String payload = payloadGenerator.generateOrderCreatedPayload(order);

    OutboxEventEntity outboxEventEntity = OutboxEventEntity.builder()
        .eventToken(UUID.randomUUID().toString())
        .entityKey(order.getId().toString())
        .payload(payload)
        .status(OutboxEventStatus.REGISTERED)
        .registeredAt(Instant.now())
        .build();

    outboxEventRepository.save(outboxEventEntity);
  }

  @Transactional
  public void appendOrderDestinationChanged(Long orderId, Destination destination) {
    String payload = payloadGenerator.generateOrderDestinationChangedPayload(orderId, destination);

    OutboxEventEntity outboxEventEntity = OutboxEventEntity.builder()
        .eventToken(UUID.randomUUID().toString())
        .entityKey(orderId.toString())
        .payload(payload)
        .status(OutboxEventStatus.REGISTERED)
        .registeredAt(Instant.now())
        .build();

    outboxEventRepository.save(outboxEventEntity);
  }

}
