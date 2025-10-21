package vroong.laas.order.core.service.infrastructure.external.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.service.domain.outbox.OutboxEvent;
import vroong.laas.order.core.service.domain.outbox.required.OutboxEventClient;

@Slf4j
@Component
public class KafkaOutboxEventClient implements OutboxEventClient {

  @Override
  public void publish(OutboxEvent event) {
    log.info("published. {}", event.getEventToken());
  }
}
