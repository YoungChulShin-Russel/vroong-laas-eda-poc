package vroong.laas.dispatch.core.domain.outbox;

import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.dispatch.core.domain.dispatch.Dispatch;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventType;

@Component
public class OutboxEventPayloadGenerator {

  public String generate(OutboxEventType eventType, Dispatch dispatch) {
    switch (eventType) {
      case DISPATCH_DISPATCHED:
        return generateDispatched(dispatch);
    }

    throw new IllegalArgumentException("invalid event type");
  }

  private String generateDispatched(Dispatch dispatch) {
    DispatchDispatchedEventPayload payload = DispatchDispatchedEventPayload.builder()
        .dispatchId(dispatch.id())
        .orderId(dispatch.orderId())
        .agentId(dispatch.agentId())
        .deliveryFee(dispatch.deliveryFee())
        .dispatchedAt(dispatch.dispatchedAt())
        .build();

    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.of(
        KafkaEventType.DISPATCH_DISPATCH_DISPATCHED,
        KafkaEventSource.DISPATCH,
        payload);

    return kafkaEvent.toJson();
  }
}
