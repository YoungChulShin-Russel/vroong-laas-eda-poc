package vroong.laas.readmodel.projection.consumer;

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
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.projection.service.ProjectionOrchestrator;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "readmodel.features.consumer.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class DeliveryEventConsumer {

    private final ProjectionOrchestrator projectionOrchestrator;

    @KafkaListener(topics = "${readmodel.topics.delivery}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleDeliveryEvent(
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
            
            log.debug("Received delivery event: eventId={}, topic={}, partition={}, offset={}", 
                    kafkaEvent.getEventId(), topic, partition, offset);

            DeliveryEvent deliveryEvent = new DeliveryEvent(kafkaEvent);
            
            // Orchestrator를 통해 처리 (agentId 매핑 포함)
            projectionOrchestrator.handleDeliveryEvent(deliveryEvent);
            
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed delivery event: eventId={}", kafkaEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process delivery event: eventId={}, error={}", 
                    kafkaEvent != null ? kafkaEvent.getEventId() : "unknown", 
                    e.getMessage(), e);
            
            
            // TODO: DLQ 또는 재시도 로직 구현
            acknowledgment.acknowledge(); // 임시로 ack 처리 (무한 재시도 방지)
        }
    }
}