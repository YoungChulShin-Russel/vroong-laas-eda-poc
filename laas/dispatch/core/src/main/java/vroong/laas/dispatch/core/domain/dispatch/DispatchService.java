package vroong.laas.dispatch.core.domain.dispatch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.dispatch.core.application.dispatch.command.RequestDispatchCommand;
import vroong.laas.dispatch.data.entity.dispatch.DispatchEntity;
import vroong.laas.dispatch.data.entity.dispatch.DispatchRepository;

@Service
@RequiredArgsConstructor
public class DispatchService {

  private final DispatchRepository dispatchRepository;

  @Transactional
  public Dispatch requestDispatch(RequestDispatchCommand command) {
    DispatchEntity dispatchEntity =
        DispatchEntity.register(command.orderId(), command.requestedAt());
    dispatchRepository.save(dispatchEntity);

    return Dispatch.fromEntity(dispatchEntity);
  }

}
