package vroong.laas.dispatch.core.infrastructure.external.outbox;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import vroong.laas.common.event.KafkaEvent;
import vroong.laas.common.event.KafkaEventPayload;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.dispatch.core.domain.outbox.OutboxEvent;
import vroong.laas.dispatch.core.domain.outbox.required.OutboxEventClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaOutboxEventClient implements OutboxEventClient {

  private final KafkaTemplate<String, String> messageRelaykafkaTemplate;

  @Override
  public void publish(OutboxEvent event)
      throws ExecutionException, InterruptedException, TimeoutException {
    KafkaEvent<KafkaEventPayload> kafkaEvent = KafkaEvent.fromJson(event.getPayload());
    KafkaEventType eventType = kafkaEvent.getType();

    messageRelaykafkaTemplate.send(
        kafkaEvent.getType().getTopic().getValue(),
        event.getPayload())
        .get(1, TimeUnit.SECONDS);

    log.info("published. {}", event.getEventToken());
  }
}
