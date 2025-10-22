package vroong.laas.delivery.core.domain.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vroong.laas.delivery.core.domain.BaseEntity;

@Getter
@Entity
@Table(name = "outbox_events")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OutboxEvent extends BaseEntity {

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

  public static OutboxEvent register(String eventToken, String entityKey, String payload) {
    return new OutboxEvent(
        eventToken,
        entityKey,
        OutboxEventStatus.REGISTERED,
        payload,
        Instant.now(),
        null);
  }

  public void markAsPublished() {
    this.status = OutboxEventStatus.PUBLISHED;
  }
}
