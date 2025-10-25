package vroong.laas.common.event.payload.dispatch;

import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;

@Builder
@Jacksonized
@Getter
public class DispatchRequestedEventPayload implements KafkaEventPayload {

  private final Long dispatchId;
  private final Long orderId;
  private final Instant requestedAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }
}
