package vroong.laas.dispatch.core.application.dispatch;

import lombok.RequiredArgsConstructor;
import vroong.laas.dispatch.core.application.dispatch.command.RequestDispatchCommand;
import vroong.laas.dispatch.core.common.annotation.UseCase;
import vroong.laas.dispatch.core.domain.dispatch.Dispatch;
import vroong.laas.dispatch.core.domain.dispatch.DispatchService;

@UseCase
@RequiredArgsConstructor
public class RequestDispatchUseCase {

  private final DispatchService dispatchService;

  public Long execute(RequestDispatchCommand command) {
    Dispatch dispatch = dispatchService.requestDispatch(command);

    return dispatch.dispatchId();
  }
}
