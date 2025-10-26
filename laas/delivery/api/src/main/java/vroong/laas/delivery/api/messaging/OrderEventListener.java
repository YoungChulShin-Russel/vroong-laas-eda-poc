package vroong.laas.delivery.api.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.order.OrderCancelledEventPayload;
import vroong.laas.delivery.core.application.delivery.DeliveryFacade;
import vroong.laas.delivery.core.domain.delivery.command.CancelDeliveryCommand;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final DeliveryFacade deliveryFacade;

  @KafkaListener(
      topics = "order.event",
      errorHandler = "kafkaErrorHandler",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handle(@Payload String payloadJson, Acknowledgment ack) {
    log.info("handleOrderEvent start, payload={}", payloadJson);
    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.fromJson(payloadJson);

    switch (kafkaEvent.getType()) {
      case ORDER_ORDER_CANCELLED -> handleOrderCancelled(kafkaEvent);
    }

    ack.acknowledge();
    log.info("handleOrderEvent end,");
  }

  private void handleOrderCancelled(KafkaEvent<KafkaEventPayload> kafkaEvent) {
    OrderCancelledEventPayload payload = (OrderCancelledEventPayload) kafkaEvent.getPayload();
    deliveryFacade.cancelDelivery(
        new CancelDeliveryCommand(payload.getOrderId(), "Order cancelled"));
  }

}
