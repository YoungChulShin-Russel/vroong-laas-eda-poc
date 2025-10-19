package vroong.laas.dispatch.core.application.dispatch.command;

import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalAction;

public record RespondProposalCommand(
    Long proposalId,
    DispatchProposalAction proposalAction
) {

}
