package vroong.laas.common.event.payload.order;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;

@Builder
@Jacksonized
@Getter
public class OrderDestinationChangedEventPayload implements KafkaEventPayload {

  private final Long orderId;
  private final OrderLocationEventDto destinationLocation;
  private final Instant changedAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }
}
