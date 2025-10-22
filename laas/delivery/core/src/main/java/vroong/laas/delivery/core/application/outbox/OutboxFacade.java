package vroong.laas.delivery.core.application.outbox;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.outbox.OutboxEventPublisher;

@Facade
@RequiredArgsConstructor
public class OutboxFacade {

  private final OutboxEventPublisher outboxEventPublisher;

  public void publish(int size) {
    outboxEventPublisher.publish(size);
  }

}
