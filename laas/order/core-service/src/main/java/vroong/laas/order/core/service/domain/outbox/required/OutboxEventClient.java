package vroong.laas.order.core.service.domain.outbox.required;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import vroong.laas.order.core.service.domain.outbox.OutboxEvent;

public interface OutboxEventClient {

  void publish(OutboxEvent event) throws ExecutionException, InterruptedException, TimeoutException;

}
