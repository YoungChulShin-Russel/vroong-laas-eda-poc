package vroong.laas.order.core.service.domain.outbox;

import java.time.Instant;
import lombok.Getter;
import vroong.laas.order.core.enums.outbox.OutboxEventStatus;
import vroong.laas.order.data.entity.outbox.OutboxEventEntity;

@Getter
public class OutboxEvent {
  private Long id;
  private String eventToken;
  private OutboxEventStatus status;
  private String payload;
  private Instant registeredAt;
  private Instant publishedAt;

  public OutboxEvent(
      Long id,
      String eventToken,
      OutboxEventStatus status,
      String payload,
      Instant registeredAt,
      Instant publishedAt) {
    this.id = id;
    this.eventToken = eventToken;
    this.status = status;
    this.payload = payload;
    this.registeredAt = registeredAt;
    this.publishedAt = publishedAt;
  }

  public static OutboxEvent fromEntity(OutboxEventEntity entity) {
    return new OutboxEvent(
        entity.getId(),
        entity.getEventToken(),
        entity.getStatus(),
        entity.getPayload(),
        entity.getRegisteredAt(),
        entity.getPublishedAt());
  }
}
