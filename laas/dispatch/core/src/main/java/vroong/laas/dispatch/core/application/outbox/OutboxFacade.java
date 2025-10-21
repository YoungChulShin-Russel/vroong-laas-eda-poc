package vroong.laas.dispatch.core.application.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.dispatch.core.domain.outbox.OutboxEventPublisher;

@Service
@RequiredArgsConstructor
public class OutboxFacade {

  private final OutboxEventPublisher outboxEventPublisher;

  public void publish(int size) {
    outboxEventPublisher.publish(size);
  }

}
