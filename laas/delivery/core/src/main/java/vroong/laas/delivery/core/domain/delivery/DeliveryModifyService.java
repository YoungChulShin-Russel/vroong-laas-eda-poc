package vroong.laas.delivery.core.domain.delivery;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.delivery.command.DeliverDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.PickupDeliveryCommand;
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
    appendHistory(delivery);

    // delivery-dispatch mapping
    DeliveryDispatchMapping deliveryDispatchMapping =
        DeliveryDispatchMapping.register(delivery.getId(), command.dispatchId());
    deliveryDispatchMappingRepository.save(deliveryDispatchMapping);

    return DeliveryInfo.fromEntity(delivery);
  }

  @Transactional
  public DeliveryInfo pickupDelivery(PickupDeliveryCommand command) {
    // pickup
    Delivery delivery = deliveryRepository.findById(command.deliveryId())
        .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다"));
    delivery.pickup();

    deliveryRepository.save(delivery);

    // history
    appendHistory(delivery);

    return DeliveryInfo.fromEntity(delivery);
  }

  @Transactional
  public DeliveryInfo deliverDelivery(DeliverDeliveryCommand command) {
    // pickup
    Delivery delivery = deliveryRepository.findById(command.deliveryId())
        .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다"));
    delivery.deliver();

    deliveryRepository.save(delivery);

    // history
    appendHistory(delivery);

    return DeliveryInfo.fromEntity(delivery);
  }

  private void appendHistory(Delivery delivery) {
    DeliveryHistory deliveryHistory = DeliveryHistory.appendNormal(delivery);
    deliveryHistoryRepository.save(deliveryHistory);
  }
}
