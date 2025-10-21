package vroong.laas.dispatch.core.domain.outbox;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.domain.dispatch.Dispatch;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventStatus;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventType;
import vroong.laas.dispatch.data.entity.outbox.OutboxEventEntity;
import vroong.laas.dispatch.data.entity.outbox.OutboxEventRepository;

@Service
@RequiredArgsConstructor
public class OutboxEventAppender {

  private final OutboxEventPayloadGenerator payloadGenerator;
  private final OutboxEventRepository outboxEventRepository;

  @Transactional
  public void append(OutboxEventType eventType, Dispatch dispatch) {
    String payload = payloadGenerator.generate(eventType, dispatch);

    OutboxEventEntity outboxEventEntity = OutboxEventEntity.builder()
        .eventToken(UUID.randomUUID().toString())
        .entityKey(dispatch.id().toString())
        .payload(payload)
        .status(OutboxEventStatus.REGISTERED)
        .registeredAt(Instant.now())
        .build();

    outboxEventRepository.save(outboxEventEntity);
  }

}
