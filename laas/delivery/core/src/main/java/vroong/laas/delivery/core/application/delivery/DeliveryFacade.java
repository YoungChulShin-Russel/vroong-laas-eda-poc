package vroong.laas.delivery.core.application.delivery;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.delivery.DeliveryModifyService;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.info.DeliveryInfo;

@Facade
@RequiredArgsConstructor
public class DeliveryFacade {

  private final DeliveryModifyService deliveryModifyService;

  public DeliveryInfo registerDelivery(RegisterDeliveryCommand command) {
    return deliveryModifyService.registerDelivery(command);
  }

}
