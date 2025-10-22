package vroong.laas.projection.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.payload.dispatch.DispatchDispatchedEventPayload;
import vroong.laas.projection.handler.DispatchProjectionHandler;
import vroong.laas.projection.model.event.DispatchEvent;
import vroong.laas.projection.model.projection.OrderProjection;
import vroong.laas.projection.service.ProjectionService;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventConsumer {

    private final DispatchProjectionHandler dispatchProjectionHandler;
    private final ProjectionService projectionService;

    @KafkaListener(topics = "${projection.topics.dispatch}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleDispatchEvent(
            @Payload KafkaEvent<DispatchDispatchedEventPayload> kafkaEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Received dispatch event: eventId={}, orderId={}, topic={}, partition={}, offset={}", 
                    kafkaEvent.getEventId(), 
                    kafkaEvent.getPayload().getOrderId(),
                    topic, partition, offset);

            DispatchEvent dispatchEvent = new DispatchEvent(kafkaEvent);
            
            // 기존 projection 조회 및 업데이트
            Optional<OrderProjection> existingProjection = projectionService.getOrderProjection(dispatchEvent.getOrderId());
            if (existingProjection.isPresent()) {
                OrderProjection updatedProjection = dispatchProjectionHandler.updateDispatchInfo(
                        existingProjection.get(), dispatchEvent);
                projectionService.saveOrderProjection(updatedProjection);
                
                log.info("Successfully updated dispatch projection: dispatchId={}, orderId={}, agentId={}", 
                        dispatchEvent.getDispatchId(), 
                        dispatchEvent.getOrderId(),
                        dispatchEvent.getAgentId());
            } else {
                log.warn("Order projection not found for dispatch event: orderId={}, dispatchId={}", 
                        dispatchEvent.getOrderId(), dispatchEvent.getDispatchId());
            }
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed dispatch event: eventId={}", kafkaEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process dispatch event: eventId={}, error={}", 
                    kafkaEvent.getEventId(), e.getMessage(), e);
            // TODO: DLQ 또는 재시도 로직 구현
            acknowledgment.acknowledge(); // 임시로 ack 처리 (무한 재시도 방지)
        }
    }
}