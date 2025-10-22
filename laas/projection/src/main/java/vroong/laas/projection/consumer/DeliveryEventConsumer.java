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
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.projection.model.event.DeliveryEvent;
import vroong.laas.projection.service.ProjectionOrchestrator;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryEventConsumer {

    private final ProjectionOrchestrator projectionOrchestrator;

    @KafkaListener(topics = "${projection.topics.delivery}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleDeliveryEvent(
            @Payload KafkaEvent<? extends KafkaEventPayload> kafkaEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        String eventTypeName = kafkaEvent.getType().name();
        
        try {
            log.debug("Received delivery event: eventId={}, topic={}, partition={}, offset={}", 
                    kafkaEvent.getEventId(), topic, partition, offset);

            DeliveryEvent deliveryEvent = new DeliveryEvent(kafkaEvent);
            
            // Orchestrator를 통해 처리 (agentId 매핑 포함)
            projectionOrchestrator.handleDeliveryEvent(deliveryEvent);
            
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed delivery event: eventId={}", kafkaEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process delivery event: eventId={}, error={}", 
                    kafkaEvent.getEventId(), e.getMessage(), e);
            
            
            // TODO: DLQ 또는 재시도 로직 구현
            acknowledgment.acknowledge(); // 임시로 ack 처리 (무한 재시도 방지)
        }
    }
}