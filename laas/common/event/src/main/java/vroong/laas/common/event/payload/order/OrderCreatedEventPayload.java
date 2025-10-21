package vroong.laas.common.event.payload.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.SchemaVersion;

@Builder
@Jacksonized
@Getter
public class OrderCreatedEventPayload implements KafkaEventPayload {

  private final Long orderId;
  private final String orderNumber;
  private final String orderStatus;
  private final OrderCreatedOrderLocation originLocation;
  private final OrderCreatedOrderLocation destinationLocation;
  private final List<OrderCreatedOrderItem> items;
  private final Instant orderedAt;

  @Override
  public SchemaVersion getSchemaVersion() {
    return new SchemaVersion(1, 0);
  }

  @Builder
  @Jacksonized
  @Getter
  public static class OrderCreatedOrderLocation {
    private final String contactName;
    private final String contactPhoneNumber;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String jibunAddress;
    private final String roadAddress;
    private final String detailAddress;
  }

  @Builder
  @Jacksonized
  @Getter
  public static class OrderCreatedOrderItem {
    private final String itemName;
    private final Integer quantity;
    private final BigDecimal price;
  }
}
