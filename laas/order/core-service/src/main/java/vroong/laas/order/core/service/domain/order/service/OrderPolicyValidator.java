package vroong.laas.order.core.service.domain.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vroong.laas.order.core.enums.order.OrderStatus;
import vroong.laas.order.core.service.common.annotation.ReadOnlyTransactional;
import vroong.laas.order.data.entity.order.OrderEntity;
import vroong.laas.order.data.entity.order.OrderRepository;

@Component
@RequiredArgsConstructor
public class OrderPolicyValidator {

  private final OrderRepository orderRepository;

  @ReadOnlyTransactional
  public void validateChangeDestination(Long orderId) {
    if (!orderRepository.existsById(orderId)) {
      throw new IllegalArgumentException("order not found");
    }
  }

  @ReadOnlyTransactional
  public void validateCancel(Long orderId) {
    OrderEntity orderEntity = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("order not found"));

    if (orderEntity.getStatus() == OrderStatus.CANCELLED) {
      throw new IllegalStateException("이미 취소된 오더입니다");
    }
  }
}
