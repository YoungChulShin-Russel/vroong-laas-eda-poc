package vroong.laas.readmodel.common.repository.mongo;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderTimelineRepository extends MongoRepository<OrderTimelineDocument, String> {
    
    /**
     * 주문 ID별 타임라인 조회 (시간순 정렬)
     */
    List<OrderTimelineDocument> findByOrderIdOrderByTimestamp(Long orderId);
}