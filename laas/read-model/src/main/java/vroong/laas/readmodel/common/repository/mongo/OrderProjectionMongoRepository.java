package vroong.laas.readmodel.common.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.time.Instant;
import java.util.List;

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
    public Mono<OrderAggregate> save(OrderAggregate projection) {
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
    public Mono<OrderAggregate> findByOrderId(Long orderId) {
        Query query = new Query(Criteria.where("orderId").is(orderId));
        
        return reactiveMongoTemplate.findOne(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
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

    /**
     * 기사별 주문 조회 (날짜 범위) - Reactive
     */
    public Mono<List<OrderAggregate>> findByAgentIdAndDateRange(Long agentId, Instant startDate, Instant endDate) {
        Criteria criteria = Criteria.where("dispatchInfo.agentId").is(agentId)
                .and("orderInfo.orderedAt").gte(startDate).lt(endDate);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders for agentId={} in date range", orders.size(), agentId))
                .doOnError(e -> log.error("Failed to find orders by agentId: agentId={}, error={}", agentId, e.getMessage()));
    }

    /**
     * 기사별 주문 조회 (날짜 범위 + 배송 상태) - Reactive
     */
    public Mono<List<OrderAggregate>> findByAgentIdAndDateRangeAndDeliveryStatus(Long agentId, Instant startDate, Instant endDate, String deliveryStatus) {
        Criteria criteria = Criteria.where("dispatchInfo.agentId").is(agentId)
                .and("orderInfo.orderedAt").gte(startDate).lt(endDate)
                .and("deliveryInfo.deliveryStatus").is(deliveryStatus);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders for agentId={} with status={}", orders.size(), agentId, deliveryStatus))
                .doOnError(e -> log.error("Failed to find orders by agentId and status: agentId={}, status={}, error={}", agentId, deliveryStatus, e.getMessage()));
    }

    /**
     * 관리자용 주문 조회 (날짜 범위) - Reactive
     */
    public Mono<List<OrderAggregate>> findByDateRange(Instant startDate, Instant endDate) {
        Criteria criteria = Criteria.where("orderInfo.orderedAt").gte(startDate).lt(endDate);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders in date range", orders.size()))
                .doOnError(e -> log.error("Failed to find orders by date range: error={}", e.getMessage()));
    }

    /**
     * 관리자용 주문 조회 (날짜 범위 + 기사 ID) - Reactive
     */
    public Mono<List<OrderAggregate>> findByDateRangeAndAgentId(Instant startDate, Instant endDate, Long agentId) {
        Criteria criteria = Criteria.where("orderInfo.orderedAt").gte(startDate).lt(endDate)
                .and("dispatchInfo.agentId").is(agentId);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders for agentId={} in date range", orders.size(), agentId))
                .doOnError(e -> log.error("Failed to find orders by date range and agentId: agentId={}, error={}", agentId, e.getMessage()));
    }

    /**
     * 관리자용 주문 조회 (날짜 범위 + 주문 상태) - Reactive
     */
    public Mono<List<OrderAggregate>> findByDateRangeAndOrderStatus(Instant startDate, Instant endDate, String orderStatus) {
        Criteria criteria = Criteria.where("orderInfo.orderedAt").gte(startDate).lt(endDate)
                .and("orderInfo.orderStatus").is(orderStatus);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders with status={} in date range", orders.size(), orderStatus))
                .doOnError(e -> log.error("Failed to find orders by date range and status: status={}, error={}", orderStatus, e.getMessage()));
    }

    /**
     * 관리자용 주문 조회 (날짜 범위 + 기사 ID + 주문 상태) - Reactive
     */
    public Mono<List<OrderAggregate>> findByDateRangeAndAgentIdAndOrderStatus(Instant startDate, Instant endDate, Long agentId, String orderStatus) {
        Criteria criteria = Criteria.where("orderInfo.orderedAt").gte(startDate).lt(endDate)
                .and("dispatchInfo.agentId").is(agentId)
                .and("orderInfo.orderStatus").is(orderStatus);
        Query query = new Query(criteria);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders for agentId={} with status={} in date range", orders.size(), agentId, orderStatus))
                .doOnError(e -> log.error("Failed to find orders by date range, agentId and status: agentId={}, status={}, error={}", agentId, orderStatus, e.getMessage()));
    }

    /**
     * 동적 조건으로 주문 조회 (유연한 쿼리 빌더) - Reactive
     */
    public Mono<List<OrderAggregate>> findOrdersByConditions(OrderSearchConditions conditions) {
        Criteria criteria = buildCriteria(conditions);
        Query query = new Query(criteria);
        
        // 성능 최적화: 정렬 및 페이징 지원
        if (conditions.getLimit() > 0) {
            query.limit(conditions.getLimit());
        }
        if (conditions.getOffset() > 0) {
            query.skip(conditions.getOffset());
        }
        
        // 기본 정렬: 최신 주문부터
        query.with(org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.DESC, "orderInfo.orderedAt"));
        
        log.debug("Executing reactive dynamic query with conditions: {}", conditions);
        
        return reactiveMongoTemplate.find(query, OrderDocument.class)
                .map(OrderDocument::toAggregate)
                .collectList()
                .doOnSuccess(orders -> log.debug("Found {} orders with dynamic conditions", orders.size()))
                .doOnError(e -> log.error("Failed to find orders with dynamic conditions: error={}", e.getMessage()));
    }
    
    private Criteria buildCriteria(OrderSearchConditions conditions) {
        Criteria criteria = new Criteria();
        
        // 날짜 범위 (필수)
        if (conditions.getStartDate() != null && conditions.getEndDate() != null) {
            criteria = criteria.and("orderInfo.orderedAt")
                    .gte(conditions.getStartDate())
                    .lt(conditions.getEndDate());
        }
        
        // 기사 ID (옵션)
        if (conditions.getAgentId() != null) {
            criteria = criteria.and("dispatchInfo.agentId").is(conditions.getAgentId());
        }
        
        // 주문 상태 (옵션)
        if (conditions.getOrderStatus() != null) {
            criteria = criteria.and("orderInfo.orderStatus").is(conditions.getOrderStatus());
        }
        
        // 배송 상태 (옵션)
        if (conditions.getDeliveryStatus() != null) {
            criteria = criteria.and("deliveryInfo.deliveryStatus").is(conditions.getDeliveryStatus());
        }
        
        return criteria;
    }
    
    /**
     * 동적 쿼리 조건을 담는 클래스 (성능 최적화 포함)
     */
    public static class OrderSearchConditions {
        private Instant startDate;
        private Instant endDate;
        private Long agentId;
        private String orderStatus;
        private String deliveryStatus;
        
        // 성능 최적화 필드
        private int limit = 100;  // 기본 제한: 100개
        private int offset = 0;   // 기본 시작: 0
        
        public OrderSearchConditions() {}
        
        public static OrderSearchConditions builder() {
            return new OrderSearchConditions();
        }
        
        public OrderSearchConditions startDate(Instant startDate) {
            this.startDate = startDate;
            return this;
        }
        
        public OrderSearchConditions endDate(Instant endDate) {
            this.endDate = endDate;
            return this;
        }
        
        public OrderSearchConditions agentId(Long agentId) {
            this.agentId = agentId;
            return this;
        }
        
        public OrderSearchConditions orderStatus(String orderStatus) {
            this.orderStatus = orderStatus;
            return this;
        }
        
        public OrderSearchConditions deliveryStatus(String deliveryStatus) {
            this.deliveryStatus = deliveryStatus;
            return this;
        }
        
        public OrderSearchConditions limit(int limit) {
            this.limit = Math.min(limit, 1000); // 최대 1000개로 제한
            return this;
        }
        
        public OrderSearchConditions offset(int offset) {
            this.offset = Math.max(offset, 0); // 음수 방지
            return this;
        }
        
        // Getters
        public Instant getStartDate() { return startDate; }
        public Instant getEndDate() { return endDate; }
        public Long getAgentId() { return agentId; }
        public String getOrderStatus() { return orderStatus; }
        public String getDeliveryStatus() { return deliveryStatus; }
        public int getLimit() { return limit; }
        public int getOffset() { return offset; }
        
        @Override
        public String toString() {
            return String.format("OrderSearchConditions{startDate=%s, endDate=%s, agentId=%s, orderStatus='%s', deliveryStatus='%s', limit=%d, offset=%d}", 
                    startDate, endDate, agentId, orderStatus, deliveryStatus, limit, offset);
        }
    }
}