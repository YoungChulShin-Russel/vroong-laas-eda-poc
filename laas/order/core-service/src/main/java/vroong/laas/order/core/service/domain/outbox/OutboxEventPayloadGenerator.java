package vroong.laas.order.core.service.domain.outbox;

import java.util.List;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderItem;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload.OrderCreatedOrderLocation;
import vroong.laas.order.core.enums.outbox.OutboxEventType;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.core.service.domain.order.Origin;

@Component
public class OutboxEventPayloadGenerator {

  public String generate(OutboxEventType eventType, Order order) {
    return switch (eventType) {
      case ORDER_CREATED -> generateOrderCreated(order);
    };
  }

  private String generateOrderCreated(Order order) {
    Origin origin = order.getLocation().getOrigin();
    OrderCreatedOrderLocation originLocation = OrderCreatedOrderLocation.builder()
        .contactName(origin.contact().name())
        .contactPhoneNumber(origin.contact().phoneNumber())
        .latitude(origin.latLng().latitude())
        .longitude(origin.latLng().longitude())
        .jibunAddress(origin.address().jibnunAddress())
        .roadAddress(origin.address().roadAddress())
        .detailAddress(origin.address().detailAddress())
        .build();

    Destination destination = order.getLocation().getDestination();
    OrderCreatedOrderLocation destinationLocation = OrderCreatedOrderLocation.builder()
        .contactName(destination.contact().name())
        .contactPhoneNumber(destination.contact().phoneNumber())
        .latitude(destination.latLng().latitude())
        .longitude(destination.latLng().longitude())
        .jibunAddress(destination.address().jibnunAddress())
        .roadAddress(destination.address().roadAddress())
        .detailAddress(destination.address().detailAddress())
        .build();

    List<OrderCreatedOrderItem> orderItems = order.getItems().stream()
        .map(item -> OrderCreatedOrderItem.builder()
            .itemName(item.itemName())
            .quantity(item.quantity())
            .price(item.price().amount())
            .build())
        .toList();

    OrderCreatedEventPayload payload = OrderCreatedEventPayload.builder()
        .orderId(order.getId())
        .orderNumber(order.getOrderNumber().value())
        .orderStatus(order.getStatus().toString())
        .originLocation(originLocation)
        .destinationLocation(destinationLocation)
        .items(orderItems)
        .orderedAt(order.getOrderedAt())
        .build();

    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.of(
        KafkaEventType.ORDER_ORDER_CREATED,
        KafkaEventSource.ORDER,
        payload);

    return kafkaEvent.toJson();
  }
}
