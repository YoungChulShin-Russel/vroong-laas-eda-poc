package vroong.laas.delivery.core.application.delivery;

import lombok.RequiredArgsConstructor;
import vroong.laas.delivery.core.common.annotation.Facade;
import vroong.laas.delivery.core.domain.delivery.DeliveryModifyService;
import vroong.laas.delivery.core.domain.delivery.agent.AgentQueryService;
import vroong.laas.delivery.core.domain.delivery.command.CancelDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.GiveUpDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.DeliverDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.PickupDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.command.RegisterDeliveryCommand;
import vroong.laas.delivery.core.domain.delivery.info.AgentInfo;
import vroong.laas.delivery.core.domain.delivery.info.DeliveryInfo;
import vroong.laas.delivery.core.domain.safenumber.SafeNumber;
import vroong.laas.delivery.core.domain.safenumber.SafeNumberPublishService;

@Facade
@RequiredArgsConstructor
public class DeliveryFacade {

  private final DeliveryModifyService deliveryModifyService;
  private final AgentQueryService agentQueryService;
  private final SafeNumberPublishService safeNumberPublishService;

  public Long registerDelivery(RegisterDeliveryCommand command) {
    // 기사 정보 조회
    AgentInfo agentInfo = agentQueryService.getAgentInfo(command.agentId());

    // 안심번호 발행
    SafeNumber safeNumber = safeNumberPublishService.publish(agentInfo.agentNumber());
    agentInfo = agentInfo.withSafeNumber(safeNumber);

    // 배송 등록
    DeliveryInfo deliveryInfo = deliveryModifyService.registerDelivery(command, agentInfo);

    return deliveryInfo.deliveryId();
  }

  public void pickupDelivery(PickupDeliveryCommand command) {
    deliveryModifyService.pickupDelivery(command);
  }

  public void deliverDelivery(DeliverDeliveryCommand command) {
    deliveryModifyService.deliverDelivery(command);
  }

  public void giveUpDelivery(GiveUpDeliveryCommand command) {
    deliveryModifyService.giveUpDelivery(command);
  }

  public void cancelDelivery(CancelDeliveryCommand command) {
    deliveryModifyService.cancelDelivery(command);
  }

}
