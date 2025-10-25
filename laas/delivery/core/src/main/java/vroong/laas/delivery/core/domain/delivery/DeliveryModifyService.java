package vroong.laas.delivery.core.domain.delivery;

import static vroong.laas.delivery.core.domain.outbox.OutboxEventType.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.delivery.core.domain.delivery.command.CancelDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.DeliverDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.PickupDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.info.DeliveryInfo;
import vroong.laas.delivery.core.domain.outbox.OutboxEventAppender;

@Service
@RequiredArgsConstructor
public class DeliveryModifyService {

  private final DeliveryNumberGenerator deliveryNumberGenerator;
  private final OutboxEventAppender outboxEventAppender;
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

    // outbox
    outboxEventAppender.append(DELIVERY_STARTED, delivery, deliveryHistory);

    return DeliveryInfo.fromEntity(delivery);
  }

  @Transactional
  public DeliveryInfo pickupDelivery(PickupDeliveryCommand command) {
    // pickup
    Delivery delivery = getDelivery(command.deliveryId());
    delivery.pickup();
    deliveryRepository.save(delivery);

    // history
    DeliveryHistory deliveryHistory = DeliveryHistory.appendNormal(delivery);
    deliveryHistoryRepository.save(deliveryHistory);

    // outbox
    outboxEventAppender.append(DELIVERY_DELIVERED, delivery, deliveryHistory);

    return DeliveryInfo.fromEntity(delivery);
  }

  @Transactional
  public DeliveryInfo deliverDelivery(DeliverDeliveryCommand command) {
    Delivery delivery = getDelivery(command.deliveryId());
    delivery.deliver();
    deliveryRepository.save(delivery);

    // history
    DeliveryHistory deliveryHistory = DeliveryHistory.appendNormal(delivery);
    deliveryHistoryRepository.save(deliveryHistory);

    // outbox
    outboxEventAppender.append(DELIVERY_DELIVERED, delivery, deliveryHistory);

    return DeliveryInfo.fromEntity(delivery);
  }

  @Transactional
  public DeliveryInfo cancelDelivery(CancelDeliveryCommand command) {
    // cancel delivery
    Delivery delivery = getDelivery(command.deliveryId());
    delivery.cancel();
    deliveryRepository.save(delivery);

    // add history
    DeliveryHistory deliveryHistory = DeliveryHistory.appendNormal(delivery, command.reason());
    deliveryHistoryRepository.save(deliveryHistory);

    // outbox
    outboxEventAppender.append(DELIVERY_CANCELLED, delivery, deliveryHistory);

    return DeliveryInfo.fromEntity(delivery);
  }

  private Delivery getDelivery(Long deliveryId) {
    return deliveryRepository.findById(deliveryId)
        .orElseThrow(() -> new IllegalArgumentException("배송 정보를 찾을 수 없습니다"));
  }
}
