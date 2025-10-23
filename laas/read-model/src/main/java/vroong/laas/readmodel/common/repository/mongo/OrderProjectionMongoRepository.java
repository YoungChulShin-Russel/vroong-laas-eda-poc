package vroong.laas.readmodel.common.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.common.model.OrderProjection;

/**
 * Reactive MongoDB Repository for Order Projection
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderProjectionMongoRepository {

    private final ReactiveMongoTemplate reactiveMongoTemplate;

    /**
     * MongoDB에 OrderProjection 저장 (Reactive)
     */
    public Mono<OrderProjection> save(OrderProjection projection) {
        OrderDocument document = OrderDocument.from(projection);
        
        return reactiveMongoTemplate.save(document)
                .doOnSuccess(saved -> log.debug("Saved order projection to MongoDB: orderId={}, id={}", 
                        projection.getOrderId(), saved.getId()))
                .doOnError(e -> log.error("Failed to save order projection to MongoDB: orderId={}, error={}", 
                        projection.getOrderId(), e.getMessage()))
                .thenReturn(projection);
    }

    /**
     * MongoDB에서 OrderProjection 조회 (Reactive)
     */
    public Mono<OrderProjection> findByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return reactiveMongoTemplate.findOne(query, OrderDocument.class)
                .map(OrderDocument::toProjection)
                .doOnNext(projection -> log.debug("Found order projection in MongoDB: orderId={}", orderId))
                .doOnError(e -> log.error("Failed to find order projection in MongoDB: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * MongoDB에서 OrderProjection 삭제 (Reactive)
     */
    public Mono<Boolean> deleteByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return reactiveMongoTemplate.remove(query, OrderDocument.class)
                .map(result -> result.getDeletedCount() > 0)
                .doOnNext(deleted -> {
                    if (deleted) {
                        log.debug("Deleted order projection from MongoDB: orderId={}", orderId);
                    }
                })
                .doOnError(e -> log.error("Failed to delete order projection from MongoDB: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * MongoDB에 OrderProjection 존재 여부 확인 (Reactive)
     */
    public Mono<Boolean> existsByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return reactiveMongoTemplate.exists(query, OrderDocument.class)
                .doOnError(e -> log.error("Failed to check existence in MongoDB: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorReturn(false);
    }
}