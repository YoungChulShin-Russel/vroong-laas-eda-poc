package vroong.laas.order.core.service.application.outbox;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.service.common.annotation.Facade;
import vroong.laas.order.core.service.domain.outbox.OutboxEventPublisher;

@Facade
@RequiredArgsConstructor
public class OutboxFacade {

  private final OutboxEventPublisher outboxEventPublisher;

  public void publish(int size) {
    outboxEventPublisher.publish(size);
  }

}
