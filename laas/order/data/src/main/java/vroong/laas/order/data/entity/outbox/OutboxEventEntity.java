package vroong.laas.order.data.entity.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumeratedValue;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.order.core.enums.outbox.OutboxEventStatus;
import vroong.laas.order.data.entity.BaseEntity;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventEntity extends BaseEntity {

  @Column(name = "event_token")
  private String eventToken;

  @Column(name = "entity_key")
  private String entityKey;

  @Column(name = "satus")
  @Enumerated(value = EnumType.STRING)
  private OutboxEventStatus status;

  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  @Column(name = "registered_at")
  private Instant registeredAt;

  @Column(name = "published_at")
  private Instant publishedAt;

  @Builder
  public OutboxEventEntity(
      String eventToken,
      String entityKey,
      String payload,
      OutboxEventStatus status,
      Instant registeredAt,
      Instant publishedAt) {
    this.eventToken = eventToken;
    this.entityKey = entityKey;
    this.payload = payload;
    this.status = status;
    this.registeredAt = registeredAt;
    this.publishedAt = publishedAt;
  }

  public void markAsPublished() {
    this.status = OutboxEventStatus.PUBLISHED;
    this.publishedAt = Instant.now();
  }
}
