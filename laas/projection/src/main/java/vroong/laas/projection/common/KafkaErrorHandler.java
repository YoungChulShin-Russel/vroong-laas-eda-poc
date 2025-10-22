package vroong.laas.projection.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, 
                            Consumer<?, ?> consumer) {
        
        log.error("Kafka message processing failed", exception);
        
        if (message.getPayload() instanceof ConsumerRecord<?, ?> record) {
            log.error("Failed message details: topic={}, partition={}, offset={}, key={}", 
                    record.topic(), record.partition(), record.offset(), record.key());
            
            // TODO: DLQ 전송 또는 재시도 로직 구현
            // 현재는 로깅만 수행하고 메시지를 버림
        }
        
        return null;
    }
}