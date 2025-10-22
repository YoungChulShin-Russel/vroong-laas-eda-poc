package vroong.laas.common.event;

import lombok.Getter;

@Getter
public enum KafkaEventSource {

  ORDER("order"),
  DISPATCH("dispatch"),
  DELIVERY("delivery"),
  ;

  private final String value;

  KafkaEventSource(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value;
  }

  public static KafkaEventSource from(String value) {
    if (value == null) {
      throw new IllegalArgumentException("EventSource value is null");
    }

    for (KafkaEventSource source : values()) {
      if (source.value.equals(value)) {
        return source;
      }
    }

    throw new IllegalArgumentException("Unknown EventSource value: " + value);
  }
}
