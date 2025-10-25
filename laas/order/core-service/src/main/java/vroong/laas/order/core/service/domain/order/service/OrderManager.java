package vroong.laas.order.core.service.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.command.CancelOrderCommand;
import vroong.laas.order.core.service.domain.order.command.ChangeDestinationCommand;
import vroong.laas.order.core.service.domain.outbox.OutboxEventAppender;
import vroong.laas.order.data.entity.order.OrderEntity;
import vroong.laas.order.data.entity.order.OrderLocationEntity;
import vroong.laas.order.data.entity.order.OrderLocationRepository;
import vroong.laas.order.data.entity.order.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderManager {

  private final OrderFinder orderFinder;
  private final OutboxEventAppender outboxEventAppender;
  private final OrderRepository orderRepository;
  private final OrderLocationRepository orderLocationRepository;

  @Transactional
  public void changeDestination(ChangeDestinationCommand command) {
    OrderLocationEntity orderLocationEntity =
        orderLocationRepository.findByOrderId(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("order id not found"));

    Destination newDestination = command.newDestination();
    orderLocationEntity.updateDestination(
        newDestination.contact().name(),
        newDestination.contact().phoneNumber(),
        newDestination.latLng().latitude(),
        newDestination.latLng().longitude(),
        newDestination.address().jibnunAddress(),
        newDestination.address().roadAddress(),
        newDestination.address().detailAddress());
    orderLocationRepository.save(orderLocationEntity);

    outboxEventAppender.appendOrderDestinationChanged(command.orderId(), newDestination);
  }

  @Transactional
  public void cancelOrder(CancelOrderCommand command) {
    OrderEntity orderEntity = orderRepository.findById(command.orderId())
        .orElseThrow(() -> new IllegalArgumentException("order id not found"));

    orderEntity.cancel();

    orderRepository.save(orderEntity);

    outboxEventAppender.appendOrderCancelled(orderEntity.getId(), orderEntity.getCancelledAt());
  }

  private void checkOrderExist(Long orderId) {
    if (orderRepository.existsById(orderId)) {
      return;
    }

    throw new IllegalArgumentException("order id not found");
  }

}
