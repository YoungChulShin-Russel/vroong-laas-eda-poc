package vroong.laas.common.event.payload.delivery;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;

@Builder
@Jacksonized
@Getter
public class DeliveryCancelledEventPayload implements KafkaEventPayload {

  private Long deliveryId;
  private Long orderId;
  private String deliveryStatus;
  private String reason;
  private Instant cancelledAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }
}
