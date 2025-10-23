package vroong.laas.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.bff.common.exception.BffException;
import vroong.laas.bff.common.exception.ErrorCode;
import vroong.laas.bff.model.OrderProjection;
import vroong.laas.bff.repository.mongo.OrderProjectionRepository;
import vroong.laas.bff.repository.redis.OrderCacheRepository;

/**
 * Order Query Service
 * Redis → MongoDB Fallback 패턴 구현 (Read-Only)
 * 
 * BFF는 조회만 담당하며, 데이터 쓰기는 Projection 서버가 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderCacheRepository cacheRepository;
    private final OrderProjectionRepository projectionRepository;

    /**
     * Order ID로 Projection 조회 (Redis → MongoDB Fallback 패턴)
     * 
     * 조회 순서:
     * 1. Redis 캐시에서 먼저 조회 (Projection 서버가 저장한 데이터)
     * 2. Redis에 없으면 MongoDB에서 조회 (Fallback)
     * 3. 둘 다 없으면 NOT_FOUND 에러
     * 
     * 참고: BFF는 Redis에 쓰기 작업을 하지 않음
     * 
     * @param orderId Order ID
     * @return OrderProjection
     */
    public Mono<OrderProjection> getOrderProjection(Long orderId) {
        return cacheRepository.findByOrderId(orderId)
                .doOnNext(projection -> 
                    log.debug("Cache HIT: Redis orderId={}", orderId))
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache MISS: Fallback to MongoDB orderId={}", orderId);
                    return fetchFromMongo(orderId);
                }))
                .switchIfEmpty(Mono.error(() -> {
                    log.warn("Order not found: orderId={}", orderId);
                    return new BffException(ErrorCode.QUERY_NOT_FOUND, "Order not found: " + orderId);
                }));
    }

    /**
     * MongoDB에서 조회 (Fallback)
     * 
     * @param orderId Order ID
     * @return OrderProjection
     */
    private Mono<OrderProjection> fetchFromMongo(Long orderId) {
        return projectionRepository.findByOrderId(orderId)
                .doOnNext(projection -> 
                    log.debug("Fallback SUCCESS: MongoDB orderId={}", orderId));
    }
}

