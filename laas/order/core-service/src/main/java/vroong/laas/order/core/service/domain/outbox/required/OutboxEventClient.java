package vroong.laas.order.core.service.domain.outbox.required;

import vroong.laas.order.core.service.domain.outbox.OutboxEvent;

public interface OutboxEventClient {

  void publish(OutboxEvent event);

}
