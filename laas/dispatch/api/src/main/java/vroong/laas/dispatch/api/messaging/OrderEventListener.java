package vroong.laas.dispatch.api.messaging;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;
import vroong.laas.dispatch.core.application.dispatch.DispatchFacade;
import vroong.laas.dispatch.core.domain.dispatch.command.RequestDispatchCommand;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventListener {

  private final DispatchFacade dispatchFacade;

  @KafkaListener(
      topics = "order.event",
      errorHandler = "kafkaErrorHandler",
      containerFactory = "kafkaListenerContainerFactory"
  )
  public void handle(@Payload String payloadJson, Acknowledgment ack) {
    log.info("handleOrderEvent start, payload={}", payloadJson);
    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.fromJson(payloadJson);

    switch (kafkaEvent.getType()) {
      case ORDER_ORDER_CREATED -> handleOrderCreated(kafkaEvent);
    }

    ack.acknowledge();
    log.info("handleOrderEvent end,");
  }

  private void handleOrderCreated(KafkaEvent<KafkaEventPayload> kafkaEvent) {
    OrderCreatedEventPayload payload = (OrderCreatedEventPayload) kafkaEvent.getPayload();
    dispatchFacade.requestDispatch(new RequestDispatchCommand(payload.getOrderId(), Instant.now()));
  }

}
