package vroong.laas.readmodel.common.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderTimelineRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * OrderTimelineDocument 저장 (Reactive)
     */
    public Mono<OrderTimelineDocument> save(OrderTimelineDocument document) {
        return reactiveMongoTemplate.save(document)
                .doOnSuccess(saved -> log.debug("Saved timeline document: orderId={}, eventType={}", 
                        document.getOrderId(), document.getEventType()))
                .doOnError(e -> log.error("Failed to save timeline document: orderId={}, error={}", 
                        document.getOrderId(), e.getMessage()));
    }

    /**
     * 주문 ID별 타임라인 조회 (시간순 정렬) - Reactive
     */
    public Mono<List<OrderTimelineDocument>> findByOrderIdOrderByTimestamp(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId))
                .with(Sort.by(Sort.Direction.ASC, "timestamp"));
        
        return reactiveMongoTemplate.find(query, OrderTimelineDocument.class)
                .collectList()
                .doOnSuccess(documents -> log.debug("Found {} timeline documents for orderId={}", 
                        documents.size(), orderId))
                .doOnError(e -> log.error("Failed to find timeline documents: orderId={}, error={}", 
                        orderId, e.getMessage()));
    }
}