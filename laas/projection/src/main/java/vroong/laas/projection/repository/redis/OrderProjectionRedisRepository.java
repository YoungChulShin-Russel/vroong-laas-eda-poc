package vroong.laas.projection.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.projection.model.projection.OrderProjection;
import vroong.laas.projection.model.redis.OrderRedisModel;

import java.time.Duration;

/**
 * Reactive Redis Repository for Order Projection
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderProjectionRedisRepository {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final Duration redisTtl;

    /**
     * Redis에 OrderProjection 저장 (Reactive)
     */
    public Mono<OrderProjection> save(OrderProjection projection) {
        String key = OrderRedisModel.generateKey(projection.getOrderId());
        OrderRedisModel redisModel = OrderRedisModel.from(projection);
        
        return reactiveRedisTemplate.opsForValue()
                .set(key, redisModel, redisTtl)
                .doOnSuccess(success -> log.debug("Saved order projection to Redis: orderId={}, key={}", 
                        projection.getOrderId(), key))
                .doOnError(e -> log.error("Failed to save order projection to Redis: orderId={}, error={}", 
                        projection.getOrderId(), e.getMessage()))
                .thenReturn(projection);
    }

    /**
     * Redis에서 OrderProjection 조회 (Reactive)
     */
    public Mono<OrderProjection> findByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(OrderRedisModel.class)
                .map(OrderRedisModel::toProjection)
                .doOnNext(projection -> log.debug("Found order projection in Redis: orderId={}", orderId))
                .doOnError(e -> log.error("Failed to find order projection in Redis: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * Redis에서 OrderProjection 삭제 (Reactive)
     */
    public Mono<Boolean> deleteByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        return reactiveRedisTemplate.delete(key)
                .map(count -> count > 0)
                .doOnNext(deleted -> {
                    if (deleted) {
                        log.debug("Deleted order projection from Redis: orderId={}", orderId);
                    } else {
                        log.debug("Order projection not found for deletion in Redis: orderId={}", orderId);
                    }
                })
                .doOnError(e -> log.error("Failed to delete order projection from Redis: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * Redis에 OrderProjection 존재 여부 확인 (Reactive)
     */
    public Mono<Boolean> existsByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        return reactiveRedisTemplate.hasKey(key)
                .doOnError(e -> log.error("Failed to check existence in Redis: orderId={}, error={}", 
                        orderId, e.getMessage()))
                .onErrorReturn(false);
    }
}