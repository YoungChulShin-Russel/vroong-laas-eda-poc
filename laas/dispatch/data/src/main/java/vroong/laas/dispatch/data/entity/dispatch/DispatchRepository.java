package vroong.laas.dispatch.data.entity.dispatch;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vroong.laas.dispatch.core.enums.DispatchStatus;

public interface DispatchRepository extends JpaRepository<DispatchEntity, Long> {

  Optional<DispatchEntity> findByOrderIdAndStatus(Long orderId, DispatchStatus status);
}
