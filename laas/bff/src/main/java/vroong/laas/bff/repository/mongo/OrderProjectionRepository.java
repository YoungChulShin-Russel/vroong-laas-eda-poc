package vroong.laas.bff.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.bff.common.exception.BffException;
import vroong.laas.bff.common.exception.ErrorCode;
import vroong.laas.bff.model.OrderProjection;

/**
 * MongoDB Reactive Repository for Order Projection
 * 
 * OrderProjection은 순수 도메인 모델 (MongoDB 어노테이션 없음)
 * ReactiveMongoTemplate이 필드명 기반으로 자동 매핑
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderProjectionRepository {

    private final ReactiveMongoTemplate mongoTemplate;
    
    private static final String COLLECTION = "order_projections";

    /**
     * Order ID로 Projection 조회
     * 
     * MongoDB 컬렉션명을 명시적으로 지정
     * 필드명 기반 자동 매핑 (orderId -> orderId, orderNumber -> orderNumber 등)
     * 
     * @param orderId Order ID
     * @return OrderProjection
     */
    public Mono<OrderProjection> findByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return mongoTemplate.findOne(query, OrderProjection.class, COLLECTION)
                .doOnNext(projection -> log.debug("Found in MongoDB: orderId={}", orderId))
                .doOnError(e -> log.error("MongoDB query failed: orderId={}, error={}", 
                        orderId, e.getMessage(), e))
                .onErrorMap(e -> new BffException(ErrorCode.MONGO_CONNECTION_ERROR, e));
    }

    /**
     * Order ID 존재 여부 확인
     * @param orderId Order ID
     * @return 존재 여부
     */
    public Mono<Boolean> existsByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return mongoTemplate.exists(query, COLLECTION)
                .doOnNext(exists -> log.debug("Exists in MongoDB: orderId={}, exists={}", 
                        orderId, exists))
                .onErrorReturn(false);
    }
}

