package vroong.laas.common.event.payload.order;

import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;
import vroong.laas.common.event.payload.dispatch.OrderItemEventDto;
import vroong.laas.common.event.payload.dispatch.OrderLocationEventDto;

@Builder
@Jacksonized
@Getter
public class OrderCreatedEventPayload implements KafkaEventPayload {

  private final Long orderId;
  private final String orderNumber;
  private final String orderStatus;
  private final OrderLocationEventDto originLocation;
  private final OrderLocationEventDto destinationLocation;
  private final List<OrderItemEventDto> items;
  private final Instant orderedAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }
}
