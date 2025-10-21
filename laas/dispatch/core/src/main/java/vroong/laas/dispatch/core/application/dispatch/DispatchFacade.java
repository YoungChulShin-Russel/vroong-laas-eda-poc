package vroong.laas.dispatch.core.application.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.dispatch.core.domain.dispatch.Dispatch;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposal;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalService;
import vroong.laas.dispatch.core.domain.dispatch.DispatchRequestService;
import vroong.laas.dispatch.core.domain.dispatch.command.ProposeDispatchCommand;
import vroong.laas.dispatch.core.domain.dispatch.command.RequestDispatchCommand;
import vroong.laas.dispatch.core.domain.dispatch.command.RespondProposalCommand;

@Service
@RequiredArgsConstructor
public class DispatchFacade {

  private final DispatchRequestService dispatchRequestService;
  private final DispatchProposalService dispatchProposalService;

  public Long requestDispatch(RequestDispatchCommand command) {
    Dispatch dispatch = dispatchRequestService.requestDispatch(command);

    return dispatch.dispatchId();
  }

  public Long proposeDispatch(ProposeDispatchCommand command) {
    DispatchProposal dispatchProposal = dispatchProposalService.proposeDispatch(command);

    return dispatchProposal.proposalId();
  }

  public void proposeRespond(RespondProposalCommand command) {
    switch (command.proposalAction()) {
      case ACCEPT -> dispatchProposalService.acceptDispatch(command.proposalId());
      case DECLINE -> dispatchProposalService.declineDispatch(command.proposalId());
    }
  }

}
