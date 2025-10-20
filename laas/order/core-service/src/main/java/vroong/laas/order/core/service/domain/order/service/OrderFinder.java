package vroong.laas.order.core.service.domain.order.service;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vroong.laas.order.core.service.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.core.service.domain.order.Order;
import vroong.laas.order.data.entity.BaseEntity;
import vroong.laas.order.data.entity.order.OrderEntity;
import vroong.laas.order.data.entity.order.OrderItemEntity;
import vroong.laas.order.data.entity.order.OrderItemRepository;
import vroong.laas.order.data.entity.order.OrderLocationEntity;
import vroong.laas.order.data.entity.order.OrderLocationRepository;
import vroong.laas.order.data.entity.order.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderFinder {

  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderLocationRepository orderLocationRepository;

  @ReadOnlyTransactional
  public Order find(Long orderId) {
    OrderEntity orderEntity = orderRepository.findById(orderId)
        .filter(BaseEntity::isActive)
        .orElseThrow(() -> new IllegalArgumentException("오더 정보를 찾을 수 없습니다"));

    List<OrderItemEntity> orderItemEntities =
        orderItemRepository.findAllByOrderId(orderEntity.getId());

    OrderLocationEntity orderLocationEntity =
        orderLocationRepository.findByOrderId(orderEntity.getId())
            .orElseThrow(() -> new IllegalArgumentException("오더 주소 정보를 찾을 수 없습니다"));

    return Order.fromEntity(orderEntity, orderItemEntities, orderLocationEntity);
  }

}
