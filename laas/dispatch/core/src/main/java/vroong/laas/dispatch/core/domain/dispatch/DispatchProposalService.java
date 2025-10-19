package vroong.laas.dispatch.core.domain.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.application.dispatch.command.ProposeDispatchCommand;
import vroong.laas.dispatch.core.enums.DispatchProposalStatus;
import vroong.laas.dispatch.core.enums.DispatchStatus;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchProposalEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchProposalRepository;
import vroong.laas.dispatch.data.entity.dispatch.DispatchRepository;

@Service
@RequiredArgsConstructor
public class DispatchProposalService {

  private final DispatchRepository dispatchRepository;
  private final DispatchProposalRepository dispatchProposalRepository;

  @Transactional
  public DispatchProposal proposeDispatch(ProposeDispatchCommand command) {
    DispatchEntity dispatchEntity =
        dispatchRepository.findByOrderIdAndStatus(command.orderId(), DispatchStatus.REQUESTED)
            .orElseThrow(() -> new IllegalArgumentException("진행 중인 배차 정보가 없습니다"));

    DispatchProposalEntity dispatchProposalEntity = DispatchProposalEntity.register(
        dispatchEntity.getId(),
        command.orderId(),
        command.agentId(),
        command.suggestedFee());
    dispatchProposalRepository.save(dispatchProposalEntity);

    return DispatchProposal.fromEntity(dispatchProposalEntity);
  }

  @Transactional
  public DispatchProposal acceptDispatch(Long proposalId) {
    DispatchProposalEntity dispatchProposalEntity = getDispatchProposal(proposalId);

    if (dispatchProposalEntity.getStatus() != DispatchProposalStatus.PROPOSED) {
      throw new IllegalStateException("배차 제안 중이 아닙니다");
    }

    dispatchProposalEntity.accept();
    dispatchProposalRepository.save(dispatchProposalEntity);

    return DispatchProposal.fromEntity(dispatchProposalEntity);
  }

  @Transactional
  public DispatchProposal declineDispatch(Long proposalId) {
    DispatchProposalEntity dispatchProposalEntity = getDispatchProposal(proposalId);

    if (dispatchProposalEntity.getStatus() != DispatchProposalStatus.PROPOSED) {
      throw new IllegalStateException("배차 제안 중이 아닙니다");
    }

    dispatchProposalEntity.decline();
    dispatchProposalRepository.save(dispatchProposalEntity);

    return DispatchProposal.fromEntity(dispatchProposalEntity);
  }

  private DispatchProposalEntity getDispatchProposal(Long proposalId) {
    return dispatchProposalRepository.findById(proposalId)
        .orElseThrow(() -> new IllegalArgumentException("배차 제안 정보가 없습니다"));
  }
}
