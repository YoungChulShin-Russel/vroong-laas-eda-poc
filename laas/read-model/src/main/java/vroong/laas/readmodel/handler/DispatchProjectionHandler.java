package vroong.laas.readmodel.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.readmodel.handler.common.DispatchEventHandler;
import vroong.laas.readmodel.model.event.DispatchEvent;
import vroong.laas.readmodel.model.projection.OrderProjection;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchProjectionHandler {

    private final List<DispatchEventHandler> dispatchEventHandlers;

    public OrderProjection updateDispatchInfo(OrderProjection existingProjection, DispatchEvent dispatchEvent) {
        log.debug("Finding handler for dispatch event type: {}", dispatchEvent.getKafkaEvent().getType());
        
        DispatchEventHandler handler = dispatchEventHandlers.stream()
                .filter(h -> h.supports(dispatchEvent.getKafkaEvent().getType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No handler found for dispatch event type: " + dispatchEvent.getKafkaEvent().getType()));
        
        log.debug("Using handler: {} for event type: {}", 
                handler.getClass().getSimpleName(), dispatchEvent.getKafkaEvent().getType());
        
        return handler.handle(existingProjection, dispatchEvent);
    }
}