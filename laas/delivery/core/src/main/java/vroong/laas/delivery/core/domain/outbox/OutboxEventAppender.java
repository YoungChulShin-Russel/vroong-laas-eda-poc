package vroong.laas.delivery.core.domain.outbox;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.delivery.Delivery;


@Service
@RequiredArgsConstructor
public class OutboxEventAppender {

  private final OutboxEventPayloadGenerator payloadGenerator;
  private final OutboxEventRepository outboxEventRepository;

  @Transactional
  public void append(OutboxEventType eventType, Delivery delivery) {
    String payload = payloadGenerator.generate(eventType, delivery);

    OutboxEvent outboxEvent = OutboxEvent.register(
        UUID.randomUUID().toString(),
        delivery.getId().toString(),
        payload);

    outboxEventRepository.save(outboxEvent);
  }

}
