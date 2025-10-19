package vroong.laas.dispatch.core.application.dispatch;

import lombok.RequiredArgsConstructor;
import vroong.laas.dispatch.core.application.dispatch.command.ProposeDispatchCommand;
import vroong.laas.dispatch.core.common.annotation.UseCase;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposal;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalService;

@UseCase
@RequiredArgsConstructor
public class ProposeDispatchUseCase {

  private final DispatchProposalService dispatchProposalService;

  public Long execute(ProposeDispatchCommand command) {
    DispatchProposal dispatchProposal = dispatchProposalService.proposeDispatch(command);

    return dispatchProposal.proposalId();
  }
}
