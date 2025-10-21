package vroong.laas.dispatch.core.domain.outbox;

import java.time.Instant;
import lombok.Getter;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventStatus;
import vroong.laas.dispatch.data.entity.outbox.OutboxEventEntity;

@Getter
public class OutboxEvent {
  private Long id;
  private String eventToken;
  private String eventKey;
  private OutboxEventStatus status;
  private String payload;
  private Instant registeredAt;
  private Instant publishedAt;

  public OutboxEvent(
      Long id,
      String eventToken,
      String eventKey,
      OutboxEventStatus status,
      String payload,
      Instant registeredAt,
      Instant publishedAt) {
    this.id = id;
    this.eventToken = eventToken;
    this.eventKey = eventKey;
    this.status = status;
    this.payload = payload;
    this.registeredAt = registeredAt;
    this.publishedAt = publishedAt;
  }

  public static OutboxEvent fromEntity(OutboxEventEntity entity) {
    return new OutboxEvent(
        entity.getId(),
        entity.getEventToken(),
        entity.getEntityKey(),
        entity.getStatus(),
        entity.getPayload(),
        entity.getRegisteredAt(),
        entity.getPublishedAt());
  }
}
