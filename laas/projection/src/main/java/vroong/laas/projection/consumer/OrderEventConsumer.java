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
import vroong.laas.common.event.payload.order.OrderCreatedEventPayload;
import vroong.laas.projection.model.event.OrderEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    @KafkaListener(topics = "${projection.topics.order}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderEvent(
            @Payload KafkaEvent<OrderCreatedEventPayload> kafkaEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            log.debug("Received order event: eventId={}, orderId={}, topic={}, partition={}, offset={}", 
                    kafkaEvent.getEventId(), 
                    kafkaEvent.getPayload().getOrderId(),
                    topic, partition, offset);

            OrderEvent orderEvent = new OrderEvent(kafkaEvent);
            
            // TODO: ProjectionHandler 호출하여 처리
            log.info("Processing order event: orderId={}, orderNumber={}, status={}", 
                    orderEvent.getOrderId(), 
                    orderEvent.getOrderNumber(),
                    orderEvent.getOrderStatus());
            
            acknowledgment.acknowledge();
            log.debug("Successfully processed order event: eventId={}", kafkaEvent.getEventId());
            
        } catch (Exception e) {
            log.error("Failed to process order event: eventId={}, error={}", 
                    kafkaEvent.getEventId(), e.getMessage(), e);
            // TODO: DLQ 또는 재시도 로직 구현
            acknowledgment.acknowledge(); // 임시로 ack 처리 (무한 재시도 방지)
        }
    }
}