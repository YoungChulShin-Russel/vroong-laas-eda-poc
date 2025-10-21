package vroong.laas.dispatch.core.domain.dispatch.command;

import vroong.laas.dispatch.core.domain.dispatch.DispatchProposalAction;

public record RespondProposalCommand(
    Long proposalId,
    DispatchProposalAction proposalAction
) {

}
