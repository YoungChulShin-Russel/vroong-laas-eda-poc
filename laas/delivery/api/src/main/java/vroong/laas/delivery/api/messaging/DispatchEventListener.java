package vroong.laas.delivery.api.messaging
    ;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.delivery.core.application.delivery.DeliveryFacade;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventListener {

  private final DeliveryFacade deliveryFacade;

  @KafkaListener(
      topics = "dispatch.event",
      errorHandler = "kafkaErrorHandler",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handle(@Payload String payloadJson, Acknowledgment ack) {
    log.info("handleOrderEvent start, payload={}", payloadJson);
    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.fromJson(payloadJson);

    switch (kafkaEvent.getType()) {
      case DISPATCH_DISPATCH_DISPATCHED -> handleDispatchDispatched(kafkaEvent);
    }

    ack.acknowledge();
    log.info("handleOrderEvent end,");
  }

  private void handleDispatchDispatched(KafkaEvent<KafkaEventPayload> kafkaEvent) {
    DispatchDispatchedEventPayload payload = (DispatchDispatchedEventPayload) kafkaEvent.getPayload();
    deliveryFacade.registerDelivery(
        new RegisterDeliveryCommand(
            payload.getDispatchId(),
            payload.getOrderId(),
            payload.getAgentId(),
            payload.getDeliveryFee()));
  }

}
