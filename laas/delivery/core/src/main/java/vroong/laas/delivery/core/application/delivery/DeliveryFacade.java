package vroong.laas.delivery.core.application.delivery;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.delivery.DeliveryModifyService;
import vroong.laas.delivery.core.domain.delivery.command.CancelDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.DeliverDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.PickupDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.info.DeliveryInfo;

@Facade
@RequiredArgsConstructor
public class DeliveryFacade {

  private final DeliveryModifyService deliveryModifyService;

  public Long registerDelivery(RegisterDeliveryCommand command) {
    DeliveryInfo deliveryInfo = deliveryModifyService.registerDelivery(command);

    return deliveryInfo.deliveryId();
  }

  public void pickupDelivery(PickupDeliveryCommand command) {
    deliveryModifyService.pickupDelivery(command);
  }

  public void deliverDelivery(DeliverDeliveryCommand command) {
    deliveryModifyService.deliverDelivery(command);
  }

  public void cancelDelivery(CancelDeliveryCommand command) {
    deliveryModifyService.cancelDelivery(command);
  }

}
