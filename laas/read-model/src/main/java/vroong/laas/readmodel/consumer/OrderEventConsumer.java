package vroong.laas.readmodel.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.readmodel.model.event.OrderEvent;
import vroong.laas.readmodel.service.ProjectionOrchestrator;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "projection.features.consumer.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OrderEventConsumer {

    private final ProjectionOrchestrator projectionOrchestrator;

    @KafkaListener(topics = "${projection.topics.order}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderEvent(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        KafkaEvent<? extends KafkaEventPayload> kafkaEvent = null;
        
        try {
            // JSON 문자열을 KafkaEvent로 역직렬화
            kafkaEvent = KafkaEvent.fromJson(message);
            
            if (kafkaEvent == null) {
                log.error("Failed to deserialize kafka event: topic={}, partition={}, offset={}", 
                        topic, partition, offset);
                acknowledgment.acknowledge();
                return;
            }
            
            OrderEvent orderEvent = new OrderEvent(kafkaEvent);
            
            log.debug("Received order event: eventId={}, orderId={}, eventType={}, topic={}, partition={}, offset={}", 
                    kafkaEvent.getEventId(), 
                    orderEvent.getOrderId(),
                    kafkaEvent.getType(),
                    topic, partition, offset);
            
            // Orchestrator를 통해 처리
            projectionOrchestrator.handleOrderEvent(orderEvent);
            
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed order event: eventId={}", kafkaEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process order event: eventId={}, error={}", 
                    kafkaEvent != null ? kafkaEvent.getEventId() : "unknown", 
                    e.getMessage(), e);
            
            
            // TODO: DLQ 또는 재시도 로직 구현
            acknowledgment.acknowledge(); // 임시로 ack 처리 (무한 재시도 방지)
        }
    }
}