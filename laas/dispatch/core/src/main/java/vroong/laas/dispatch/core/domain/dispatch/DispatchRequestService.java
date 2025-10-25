package vroong.laas.dispatch.core.domain.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.domain.dispatch.command.RequestDispatchCommand;
import vroong.laas.dispatch.core.domain.outbox.OutboxEventAppender;
import vroong.laas.dispatch.core.enums.dispatch.DispatchStatus;
import vroong.laas.dispatch.core.enums.outbox.OutboxEventType;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchRepository;

@Service
@RequiredArgsConstructor
public class DispatchRequestService {

  private final OutboxEventAppender outboxEventAppender;
  private final DispatchRepository dispatchRepository;

  @Transactional
  public Dispatch requestDispatch(RequestDispatchCommand command) {
    if (dispatchRepository.existsByOrderIdAndStatus(command.orderId(), DispatchStatus.REQUESTED)) {
      throw new IllegalStateException("진행중인 배차가 있습니다");
    }

    DispatchEntity dispatchEntity =
        DispatchEntity.register(command.orderId(), command.requestedAt());
    dispatchRepository.save(dispatchEntity);

    Dispatch dispatch = Dispatch.fromEntity(dispatchEntity);

    outboxEventAppender.append(OutboxEventType.DISPATCH_REQUESTED, dispatch);

    return dispatch;
  }

}
