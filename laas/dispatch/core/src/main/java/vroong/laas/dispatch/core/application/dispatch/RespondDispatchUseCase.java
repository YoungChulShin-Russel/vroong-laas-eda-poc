package vroong.laas.dispatch.core.application.dispatch;

import lombok.RequiredArgsConstructor;
import vroong.laas.dispatch.core.application.dispatch.command.RespondDispatchCommand;
import vroong.laas.dispatch.core.common.annotation.UseCase;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalService;

@UseCase
@RequiredArgsConstructor
public class RespondDispatchUseCase {

  private final DispatchProposalService dispatchProposalService;

  public void execute(RespondDispatchCommand command) {
    switch (command.proposalAction()) {
      case ACCEPT -> dispatchProposalService.acceptDispatch(command.proposalId());
      case DECLINE -> dispatchProposalService.declineDispatch(command.proposalId());
    }
  }
}
