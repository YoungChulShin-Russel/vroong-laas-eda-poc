package vroong.laas.common.event;

import lombok.Getter;

@Getter
public enum KafkaEventTopic {

  ORDER_EVENT("order.event"),
  DISPATCH_EVENT("dispatch.event"),
  DELIVERY_EVENT("delivery.event")
  ;

  private final String value;

  KafkaEventTopic(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static KafkaEventTopic from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("EventTopic value is null");
    }

    for (KafkaEventTopic topic : values()) {
      if (topic.value.equals(value)) {
        return topic;
      }
    }

    throw new IllegalArgumentException("Unknown EventTopic value: " + value);
  }
}
