package vroong.laas.dispatch.api.web.dispatch.request;

import java.time.Instant;
import vroong.laas.dispatch.core.domain.dispatch.command.RequestDispatchCommand;

public record RequestDispatchRequest(
    Long orderId,
    Instant requestedAt
) {

  public RequestDispatchCommand toCommand() {
    return new RequestDispatchCommand(this.orderId, this.requestedAt);
  }

}
