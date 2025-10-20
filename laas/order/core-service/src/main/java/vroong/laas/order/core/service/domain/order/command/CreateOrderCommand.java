package vroong.laas.order.core.service.domain.order.command;

import java.util.List;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.NewOrderItem;
import vroong.laas.order.core.service.domain.order.Origin;

public record CreateOrderCommand(
    List<NewOrderItem> items,
    Origin origin,
    Destination destination) {

  public CreateOrderCommand {
    if (origin == null) {
      throw new IllegalArgumentException("출발지는 필수입니다");
    }
    if (destination == null) {
      throw new IllegalArgumentException("도착지는 필수입니다");
    }
  }
}

