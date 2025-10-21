package vroong.laas.dispatch.core.domain.dispatch;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.domain.dispatch.command.ProposeDispatchCommand;
import vroong.laas.dispatch.core.domain.outbox.OutboxEventAppender;
import vroong.laas.dispatch.core.enums.dispatch.DispatchProposalStatus;
import vroong.laas.dispatch.core.enums.dispatch.DispatchStatus;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventType;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchProposalEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchProposalRepository;
import vroong.laas.dispatch.data.entity.dispatch.DispatchRepository;

@Service
@RequiredArgsConstructor
public class DispatchProposalService {

  private final OutboxEventAppender outboxEventAppender;
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
        command.suggestedFee(),
        Instant.now().plusSeconds(30));
    dispatchProposalRepository.save(dispatchProposalEntity);

    DispatchProposal dispatchProposal = DispatchProposal.fromEntity(dispatchProposalEntity);
    Dispatch dispatch = Dispatch.fromEntity(dispatchEntity);

    outboxEventAppender.append(OutboxEventType.DISPATCH_DISPATCHED, dispatch);

    return dispatchProposal;
  }

  @Transactional
  public DispatchProposal acceptDispatch(Long proposalId) {
    DispatchProposalEntity dispatchProposalEntity = getDispatchProposal(proposalId);

    if (dispatchProposalEntity.getStatus() != DispatchProposalStatus.PROPOSED) {
      throw new IllegalStateException("배차 제안 중이 아닙니다");
    }

    dispatchProposalEntity.accept();
    dispatchProposalRepository.save(dispatchProposalEntity);

    DispatchEntity dispatchEntity = dispatchRepository.findById(
            dispatchProposalEntity.getDispatchId())
        .orElseThrow(() -> new IllegalArgumentException("배차 요청 정보를 찾을 수 없습니다"));

    dispatchEntity.dispatch(
        dispatchProposalEntity.getAgentId(),
        dispatchProposalEntity.getSuggestedFee(),
        dispatchProposalEntity.getRespondedAt());
    dispatchRepository.save(dispatchEntity);

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
