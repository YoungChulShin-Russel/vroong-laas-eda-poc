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
import vroong.laas.projection.model.event.DispatchEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class DispatchEventConsumer {

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
            
            // TODO: ProjectionHandler 호출하여 처리
            log.info("Processing dispatch event: dispatchId={}, orderId={}, agentId={}, deliveryFee={}", 
                    dispatchEvent.getDispatchId(), 
                    dispatchEvent.getOrderId(),
                    dispatchEvent.getAgentId(),
                    dispatchEvent.getDeliveryFee());
            
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