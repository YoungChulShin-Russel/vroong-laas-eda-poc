package vroong.laas.dispatch.core.domain.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.application.dispatch.command.ProposeDispatchCommand;
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
}
