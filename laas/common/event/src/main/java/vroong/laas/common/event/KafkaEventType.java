package vroong.laas.common.event;

import lombok.Getter;
import vroong.laas.common.event.payload.delivery.DeliveryDeliveredEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;

@Getter
public enum KafkaEventType {

  ORDER_ORDER_CREATED(
      "order.order.created",
      KafkaEventTopic.ORDER_EVENT,
      OrderCreatedEventPayload.class),

  DISPATCH_DISPATCH_DISPATCHED(
      "dispatch.dispatch.dispatched",
      KafkaEventTopic.DISPATCH_EVENT,
      DispatchDispatchedEventPayload.class),

  DELIVERY_DELIVERY_DELIVERED(
      "delivery.delivery.delivered",
      KafkaEventTopic.DELIVERY_EVENT,
      DeliveryDeliveredEventPayload.class)
  ;

  private final String value;
  private final Class<? extends KafkaEventPayload> payloadClass;
  private final KafkaEventTopic topic;

  KafkaEventType(
      String value,
      KafkaEventTopic topic,
      Class<? extends KafkaEventPayload> payloadClass) {
    this.value = value;
    this.topic = topic;
    this.payloadClass = payloadClass;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static KafkaEventType from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("EventType value is null");
    }

    for (KafkaEventType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }

    throw new IllegalArgumentException("Unknown EventType value: " + value);
  }
}
