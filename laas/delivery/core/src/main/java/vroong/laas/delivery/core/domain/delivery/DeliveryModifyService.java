package vroong.laas.delivery.core.domain.delivery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.info.DeliveryInfo;

@Service
@RequiredArgsConstructor
public class DeliveryModifyService {

  private final DeliveryNumberGenerator deliveryNumberGenerator;
  private final DeliveryRepository deliveryRepository;
  private final DeliveryDispatchMappingRepository deliveryDispatchMappingRepository;
  private final DeliveryHistoryRepository deliveryHistoryRepository;

  @Transactional
  public DeliveryInfo registerDelivery(RegisterDeliveryCommand command) {
    // delivery
    Delivery delivery = Delivery.register(command, deliveryNumberGenerator);
    deliveryRepository.save(delivery);

    // delivery history
    DeliveryHistory deliveryHistory = DeliveryHistory.appendNormal(delivery);
    deliveryHistoryRepository.save(deliveryHistory);

    // delivery-dispatch mapping
    DeliveryDispatchMapping deliveryDispatchMapping =
        DeliveryDispatchMapping.register(delivery.getId(), command.dispatchId());
    deliveryDispatchMappingRepository.save(deliveryDispatchMapping);

    return DeliveryInfo.fromEntity(delivery);
  }
}
