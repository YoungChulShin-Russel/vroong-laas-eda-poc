package vroong.laas.dispatch.api.web.dispatch.request;

import jakarta.validation.constraints.NotNull;
import vroong.laas.dispatch.core.domain.dispatch.command.RespondProposalCommand;
import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalAction;

public record RespondProposalRequest(
    @NotNull Long proposalId,
    @NotNull String action
) {

  public RespondProposalCommand toCommand() {
    return new RespondProposalCommand(
        this.proposalId,
        DispatchProposalAction.valueOf(this.action));
  }
}
