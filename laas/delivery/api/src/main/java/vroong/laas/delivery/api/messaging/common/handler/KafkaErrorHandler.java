package vroong.laas.delivery.api.messaging.common.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.ConsumerAwareListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaErrorHandler implements ConsumerAwareListenerErrorHandler {

  @Override
  public Object handleError(
      Message<?> message,
      ListenerExecutionFailedException exception,
      @Nullable Consumer<?, ?> consumer) {
    log.error("KafkaErrorHandler handleError. message: {}", message, exception);
    return null;
  }
}
