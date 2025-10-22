package vroong.laas.delivery.core.domain.outbox.required;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import vroong.laas.delivery.core.domain.outbox.OutboxEvent;

public interface OutboxEventClient {

  void publish(OutboxEvent event) throws ExecutionException, InterruptedException, TimeoutException;

}
