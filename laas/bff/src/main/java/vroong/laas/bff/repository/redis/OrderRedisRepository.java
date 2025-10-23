package vroong.laas.bff.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.bff.model.OrderProjection;

/**
 * Redis Reactive Repository for Order Projection Cache (Read-Only)
 * Projection 서버가 이미 Redis에 저장한 데이터를 조회만 함
 * BFF는 쓰기 작업을 하지 않음
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    private static final String KEY_PREFIX = "order:projection:";

    /**
     * Redis 캐시 키 생성
     */
    private String generateKey(Long orderId) {
        return KEY_PREFIX + orderId;
    }

    /**
     * Order ID로 캐시된 Projection 조회
     * @param orderId Order ID
     * @return OrderProjection (없으면 empty)
     */
    public Mono<OrderProjection> findByOrderId(Long orderId) {
        String key = generateKey(orderId);
        
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .cast(OrderProjection.class)
                .doOnNext(projection -> log.debug("Found order projection in Redis cache: orderId={}", orderId))
                .doOnError(e -> log.error("Failed to get order projection from Redis: orderId={}, error={}", 
                        orderId, e.getMessage(), e))
                .onErrorResume(e -> {
                    log.warn("Redis error occurred, returning empty: orderId={}", orderId);
                    return Mono.empty();
                });
    }

    /**
     * 캐시 존재 여부 확인 (선택적 기능)
     * @param orderId Order ID
     * @return 존재 여부
     */
    public Mono<Boolean> existsByOrderId(Long orderId) {
        String key = generateKey(orderId);
        
        return reactiveRedisTemplate.hasKey(key)
                .defaultIfEmpty(false)
                .doOnNext(exists -> log.debug("Order projection exists in Redis: orderId={}, exists={}", 
                        orderId, exists))
                .onErrorReturn(false);
    }
}

