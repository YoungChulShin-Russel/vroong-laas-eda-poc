package vroong.laas.delivery.core.domain.outbox;

import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_DELIVERED;
import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_PICKED_UP;
import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_STARTED;

import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.delivery.DeliveryDeliveredEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryPickedUpEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryStartedEventPayload;
import vroong.laas.delivery.core.domain.delivery.Delivery;

@Component
public class OutboxEventPayloadGenerator {

  public String generate(OutboxEventType eventType, Delivery delivery) {
    return switch (eventType) {
      case DELIVERY_STARTED -> generateDeliveryStartedPayload(delivery);
      case DELIVERY_PICKED_UP -> generateDeliveryPickedUpPayload(delivery);
      case DELIVERY_DELIVERED -> generateDeliveryDeliveredPayload(delivery);
    };

  }

  private String generateDeliveryStartedPayload(Delivery delivery) {
    var payload = DeliveryStartedEventPayload.builder()
        .deliveryId(delivery.getId())
        .agentId(delivery.getAgentId())
        .occurredAt(delivery.getCreatedAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_STARTED, payload);

    return kafkaEvent.toJson();
  }

  private String generateDeliveryPickedUpPayload(Delivery delivery) {
    var payload = DeliveryPickedUpEventPayload.builder()
        .deliveryId(delivery.getId())
        .agentId(delivery.getAgentId())
        .occurredAt(delivery.getCreatedAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_PICKED_UP, payload);

    return kafkaEvent.toJson();
  }

  private String generateDeliveryDeliveredPayload(Delivery delivery) {
    var payload = DeliveryDeliveredEventPayload.builder()
        .deliveryId(delivery.getId())
        .agentId(delivery.getAgentId())
        .occurredAt(delivery.getCreatedAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_DELIVERED, payload);

    return kafkaEvent.toJson();
  }

  private KafkaEvent<KafkaEventPayload> getKafkaEvent(
      KafkaEventType eventType,
      KafkaEventPayload payload) {
    return KafkaEvent.of(
        eventType,
        KafkaEventSource.DELIVERY,
        payload);
  }
}
