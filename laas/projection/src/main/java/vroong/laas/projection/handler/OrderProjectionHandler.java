package vroong.laas.projection.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.projection.handler.common.OrderEventHandler;
import vroong.laas.projection.model.event.OrderEvent;
import vroong.laas.projection.model.projection.OrderProjection;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProjectionHandler {

    private final List<OrderEventHandler> orderEventHandlers;

    public OrderProjection handleOrderEvent(OrderEvent orderEvent) {
        log.debug("Finding handler for order event type: {}", orderEvent.getKafkaEvent().getType());
        
        OrderEventHandler handler = orderEventHandlers.stream()
                .filter(h -> h.supports(orderEvent.getKafkaEvent().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No handler found for order event type: " + orderEvent.getKafkaEvent().getType()));
        
        log.debug("Using handler: {} for event type: {}", 
                handler.getClass().getSimpleName(), orderEvent.getKafkaEvent().getType());
        
        return handler.handle(orderEvent);
    }
}