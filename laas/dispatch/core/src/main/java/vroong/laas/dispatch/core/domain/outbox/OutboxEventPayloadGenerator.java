package vroong.laas.dispatch.core.domain.outbox;

import static vroong.laas.common.event.KafkaEventType.DISPATCH_DISPATCH_DISPATCHED;
import static vroong.laas.common.event.KafkaEventType.DISPATCH_DISPATCH_REQUESTED;

import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventSource;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.common.event.payload.dispatch.DispatchRequestedEventPayload;
import vroong.laas.dispatch.core.domain.dispatch.Dispatch;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventType;

@Component
public class OutboxEventPayloadGenerator {


  public String generate(OutboxEventType eventType, Dispatch dispatch) {
    return switch (eventType) {
      case DISPATCH_REQUESTED -> generateRequested(dispatch);
      case DISPATCH_DISPATCHED -> generateDispatched(dispatch);
    };
  }

  private KafkaEvent<KafkaEventPayload> generateKafkaEvent(
      KafkaEventType eventType,
      KafkaEventPayload payload) {
    return KafkaEvent.of(eventType, KafkaEventSource.DISPATCH, payload);
  }

  private String generateRequested(Dispatch dispatch) {
    DispatchRequestedEventPayload payload = DispatchRequestedEventPayload.builder()
        .dispatchId(dispatch.id())
        .orderId(dispatch.orderId())
        .requestedAt(dispatch.requestedAt())
        .build();

    return generateKafkaEvent(DISPATCH_DISPATCH_REQUESTED, payload).toJson();
  }

  private String generateDispatched(Dispatch dispatch) {
    DispatchDispatchedEventPayload payload = DispatchDispatchedEventPayload.builder()
        .dispatchId(dispatch.id())
        .orderId(dispatch.orderId())
        .agentId(dispatch.agentId())
        .deliveryFee(dispatch.deliveryFee())
        .dispatchedAt(dispatch.dispatchedAt())
        .build();

    return generateKafkaEvent(DISPATCH_DISPATCH_DISPATCHED, payload).toJson();
  }
}
