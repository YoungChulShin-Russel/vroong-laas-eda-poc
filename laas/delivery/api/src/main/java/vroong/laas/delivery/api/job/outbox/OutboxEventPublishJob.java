package vroong.laas.delivery.api.job.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vroong.laas.delivery.api.job.BaseScheduledJob;
import vroong.laas.delivery.core.application.outbox.OutboxFacade;

@Component
@RequiredArgsConstructor
public class OutboxEventPublishJob implements BaseScheduledJob {

  private final OutboxFacade outboxFacade;

  @Scheduled(fixedDelayString = "10000")
  @Override
  public void execute() {
    outboxFacade.publish(10);
  }
}
