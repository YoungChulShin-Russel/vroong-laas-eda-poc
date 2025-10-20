package vroong.laas.order.core.service.application.order;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.service.common.annotation.Facade;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.core.service.domain.order.OrderNumber;
import vroong.laas.order.core.service.domain.order.OrderNumberGenerator;
import vroong.laas.order.core.service.domain.order.Origin;
import vroong.laas.order.core.service.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.service.domain.order.service.OrderAddressRefiner;
import vroong.laas.order.core.service.domain.order.service.OrderCreator;

@Facade
@RequiredArgsConstructor
public class OrderFacade {

  private final OrderNumberGenerator orderNumberGenerator;
  private final OrderCreator orderCreator;
  private final OrderAddressRefiner orderAddressRefiner;

  public Long createOrder(CreateOrderCommand command) {
    Origin refinedOrigin = orderAddressRefiner.refineOrigin(command.origin());
    Destination refinedDestination = orderAddressRefiner.refineDestination(command.destination());

    OrderNumber orderNumber = orderNumberGenerator.generate();

    Order order =
        orderCreator.create(orderNumber, command.items(), refinedOrigin, refinedDestination);

    return order.getId();
  }
}
