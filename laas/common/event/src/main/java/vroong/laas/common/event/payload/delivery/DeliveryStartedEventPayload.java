package vroong.laas.common.event.payload.delivery;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;

@Builder
@Jacksonized
@Getter
public class DeliveryStartedEventPayload implements KafkaEventPayload {

  private Long deliveryId;
  private String deliveryNumber;
  private Long orderId;
  private Long agentId;
  private BigDecimal deliveryFee;
  private String deliveryStatus;
  private Instant startedAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }
}
