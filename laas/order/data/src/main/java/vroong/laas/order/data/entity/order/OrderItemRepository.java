package vroong.laas.order.data.entity.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {

  List<OrderItemEntity> findAllByOrderId(Long orderId);
}
