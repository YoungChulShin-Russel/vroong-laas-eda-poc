package vroong.laas.dispatch.api.job.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import vroong.laas.dispatch.api.job.BaseScheduledJob;
import vroong.laas.dispatch.core.application.outbox.OutboxFacade;

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
