package vroong.laas.readmodel.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.readmodel.handler.common.DeliveryEventHandler;
import vroong.laas.readmodel.model.event.DeliveryEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryProjectionHandler {

    private final List<DeliveryEventHandler> deliveryEventHandlers;

    public OrderProjection updateDeliveryStatus(OrderProjection existingProjection, DeliveryEvent deliveryEvent) {
        log.debug("Finding handler for delivery event type: {}", deliveryEvent.getKafkaEvent().getType());
        
        DeliveryEventHandler handler = deliveryEventHandlers.stream()
                .filter(h -> h.supports(deliveryEvent.getKafkaEvent().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No handler found for delivery event type: " + deliveryEvent.getKafkaEvent().getType()));
        
        log.debug("Using handler: {} for event type: {}", 
                handler.getClass().getSimpleName(), deliveryEvent.getKafkaEvent().getType());
        
        return handler.handle(existingProjection, deliveryEvent);
    }
}