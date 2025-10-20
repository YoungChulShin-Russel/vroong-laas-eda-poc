package vroong.laas.order.data.entity.order;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderLocationRepository extends JpaRepository<OrderLocationEntity, Long> {

  Optional<OrderLocationEntity> findByOrderId(Long orderId);
}
