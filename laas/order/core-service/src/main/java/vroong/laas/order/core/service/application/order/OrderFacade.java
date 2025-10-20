package vroong.laas.order.core.service.application.order;

import lombok.RequiredArgsConstructor;
import vroong.laas.order.core.service.common.annotation.Facade;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.core.service.domain.order.Origin;
import vroong.laas.order.core.service.domain.order.command.CreateOrderCommand;
import vroong.laas.order.core.service.domain.order.query.GetOrderQuery;
import vroong.laas.order.core.service.domain.order.service.OrderAddressRefiner;
import vroong.laas.order.core.service.domain.order.service.OrderCreator;
import vroong.laas.order.core.service.domain.order.service.OrderFinder;

@Facade
@RequiredArgsConstructor
public class OrderFacade {

  private final OrderCreator orderCreator;
  private final OrderFinder orderFinder;
  private final OrderAddressRefiner orderAddressRefiner;

  public Long createOrder(CreateOrderCommand command) {
    Origin refinedOrigin = orderAddressRefiner.refineOrigin(command.origin());
    Destination refinedDestination = orderAddressRefiner.refineDestination(command.destination());

    Order order = orderCreator.create(command.items(), refinedOrigin, refinedDestination);

    return order.getId();
  }

  public Order getOrder(GetOrderQuery query) {
    return orderFinder.find(query.orderId());
  }
}
