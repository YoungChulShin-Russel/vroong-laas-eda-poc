package vroong.laas.order.core.service.domain.order.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vroong.laas.order.core.enums.order.OrderStatus;
import vroong.laas.order.core.enums.outbox.OutboxEventType;
import vroong.laas.order.core.service.domain.order.Destination;
import vroong.laas.order.core.service.domain.order.NewOrderItem;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.core.service.domain.order.OrderNumber;
import vroong.laas.order.core.service.domain.order.OrderNumberGenerator;
import vroong.laas.order.core.service.domain.order.Origin;
import vroong.laas.order.core.service.domain.outbox.OutboxEventAppender;
import vroong.laas.order.data.entity.order.OrderEntity;
import vroong.laas.order.data.entity.order.OrderItemEntity;
import vroong.laas.order.data.entity.order.OrderItemRepository;
import vroong.laas.order.data.entity.order.OrderLocationEntity;
import vroong.laas.order.data.entity.order.OrderLocationRepository;
import vroong.laas.order.data.entity.order.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderCreator {

  private final OrderNumberGenerator orderNumberGenerator;
  private final OutboxEventAppender outboxEventAppender;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderLocationRepository orderLocationRepository;

  @Transactional
  public Order create(
      List<NewOrderItem> items,
      Origin origin,
      Destination destination
  ) {
    // generate order number
    OrderNumber orderNumber = orderNumberGenerator.generate();

    // save order
    OrderEntity orderEntity = OrderEntity.builder()
        .orderNumber(orderNumber.value())
        .status(OrderStatus.CREATED)
        .orderedAt(Instant.now())
        .build();

    orderRepository.save(orderEntity);

    // save order items
    List<OrderItemEntity> orderItemEntities = orderItemRepository.saveAll(
        items.stream()
            .map(item -> OrderItemEntity.builder()
                .orderId(orderEntity.getId())
                .itemName(item.itemName())
                .quantity(item.quantity())
                .price(item.price().amount())
                .build())
            .toList());

    // save order location
    OrderLocationEntity orderLocationEntity = OrderLocationEntity.builder()
        .orderId(orderEntity.getId())
        .originContactName(origin.contact().name())
        .originContactPhoneNumber(origin.contact().phoneNumber())
        .originLatitude(origin.latLng().latitude())
        .originLongitude(origin.latLng().longitude())
        .originJibnunAddress(origin.address().jibnunAddress())
        .originRoadAddress(origin.address().roadAddress())
        .originDetailAddress(origin.address().detailAddress())
        .destinationContactName(destination.contact().name())
        .destinationContactPhoneNumber(destination.contact().phoneNumber())
        .destinationLatitude(destination.latLng().latitude())
        .destinationLongitude(destination.latLng().longitude())
        .destinationJibnunAddress(destination.address().jibnunAddress())
        .destinationRoadAddress(destination.address().roadAddress())
        .destinationDetailAddress(destination.address().detailAddress())
        .build();

    orderLocationRepository.save(orderLocationEntity);

    // map
    Order order = Order.fromEntity(orderEntity, orderItemEntities, orderLocationEntity);

    // save outbox
    outboxEventAppender.append(OutboxEventType.ORDER_CREATED, order);

    return order;
  }
}
