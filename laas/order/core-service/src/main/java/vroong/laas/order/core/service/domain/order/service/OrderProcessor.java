package vroong.laas.order.core.service.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.command.ChangeDestinationAddressCommand;
import vroong.laas.order.core.service.domain.outbox.OutboxEventAppender;
import vroong.laas.order.data.entity.order.OrderLocationEntity;
import vroong.laas.order.data.entity.order.OrderLocationRepository;
import vroong.laas.order.data.entity.order.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderProcessor {

  private final OutboxEventAppender outboxEventAppender;
  private final OrderRepository orderRepository;
  private final OrderLocationRepository orderLocationRepository;

  public void changeDestination(ChangeDestinationAddressCommand command) {
    if (!orderRepository.existsById(command.orderId())) {
      throw new IllegalArgumentException("order id not found");
    }

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

}
