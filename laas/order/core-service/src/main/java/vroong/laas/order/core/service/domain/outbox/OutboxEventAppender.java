package vroong.laas.order.core.service.domain.outbox;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.enums.outbox.OutboxEventStatus;
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

    OutboxEventEntity outboxEventEntity = getOutboxEventEntity(order.getId(), payload);

    outboxEventRepository.save(outboxEventEntity);
  }

  @Transactional
  public void appendOrderDestinationChanged(Long orderId, Destination destination) {
    String payload = payloadGenerator.generateOrderDestinationChangedPayload(orderId, destination);

    OutboxEventEntity outboxEventEntity = getOutboxEventEntity(orderId, payload);

    outboxEventRepository.save(outboxEventEntity);
  }

  @Transactional
  public void appendOrderCancelled(Long orderId, Instant cancelledAt) {
    String payload = payloadGenerator.generateOrderCancelledPayload(orderId, cancelledAt);

    OutboxEventEntity outboxEventEntity = getOutboxEventEntity(orderId, payload);

    outboxEventRepository.save(outboxEventEntity);
  }



  private OutboxEventEntity getOutboxEventEntity(Long orderId, String payload) {
    return OutboxEventEntity.builder()
        .eventToken(UUID.randomUUID().toString())
        .entityKey(orderId.toString())
        .payload(payload)
        .status(OutboxEventStatus.REGISTERED)
        .registeredAt(Instant.now())
        .build();
  }

}
