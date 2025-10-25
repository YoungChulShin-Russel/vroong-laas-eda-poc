package vroong.laas.delivery.core.domain.outbox;

import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_DELIVERED;
import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_PICKED_UP;
import static vroong.laas.common.event.KafkaEventType.DELIVERY_DELIVERY_STARTED;

import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.delivery.DeliveryCancelledEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryDeliveredEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryPickedUpEventPayload;
import vroong.laas.common.event.payload.delivery.DeliveryStartedEventPayload;
import vroong.laas.delivery.core.domain.delivery.Delivery;
import vroong.laas.delivery.core.domain.delivery.DeliveryHistory;

@Component
public class OutboxEventPayloadGenerator {

  public String generate(OutboxEventType eventType, Delivery delivery, DeliveryHistory history) {
    return switch (eventType) {
      case DELIVERY_STARTED -> generateDeliveryStartedPayload(delivery, history);
      case DELIVERY_PICKED_UP -> generateDeliveryPickedUpPayload(delivery, history);
      case DELIVERY_DELIVERED -> generateDeliveryDeliveredPayload(delivery, history);
      case DELIVERY_CANCELLED -> generateDeliveryCancelledPayload(delivery, history);
    };

  }

  private String generateDeliveryStartedPayload(Delivery delivery, DeliveryHistory history) {
    var payload = DeliveryStartedEventPayload.builder()
        .deliveryId(delivery.getId())
        .deliveryNumber(delivery.getDeliveryNumber().value())
        .orderId(delivery.getOrderId())
        .agentId(delivery.getAgentId())
        .deliveryFee(delivery.getDeliveryFee())
        .deliveryStatus(delivery.getStatus().name())
        .startedAt(history.getRegisteredAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_STARTED, payload);

    return kafkaEvent.toJson();
  }

  private String generateDeliveryPickedUpPayload(Delivery delivery, DeliveryHistory history) {
    var payload = DeliveryPickedUpEventPayload.builder()
        .deliveryId(delivery.getId())
        .orderId(delivery.getOrderId())
        .deliveryStatus(delivery.getStatus().name())
        .pickedUpAt(history.getRegisteredAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_PICKED_UP, payload);

    return kafkaEvent.toJson();
  }

  private String generateDeliveryDeliveredPayload(Delivery delivery, DeliveryHistory history) {
    var payload = DeliveryDeliveredEventPayload.builder()
        .deliveryId(delivery.getId())
        .orderId(delivery.getOrderId())
        .deliveryStatus(delivery.getStatus().name())
        .deliveredAt(history.getRegisteredAt())
        .build();

    var kafkaEvent = getKafkaEvent(DELIVERY_DELIVERY_DELIVERED, payload);

    return kafkaEvent.toJson();
  }

  private String generateDeliveryCancelledPayload(Delivery delivery, DeliveryHistory history) {
    var payload = DeliveryCancelledEventPayload.builder()
        .deliveryId(delivery.getId())
        .orderId(delivery.getOrderId())
        .deliveryStatus(delivery.getStatus().name())
        .reason(history.getReason())
        .cancelledAt(history.getRegisteredAt())
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
