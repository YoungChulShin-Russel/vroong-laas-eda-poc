package vroong.laas.projection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.projection.client.OrderServiceClient;
import vroong.laas.projection.dto.OrderServiceResponse;
import vroong.laas.projection.exception.OrderNotFoundException;
import vroong.laas.projection.model.projection.OrderProjection;
import vroong.laas.projection.repository.mongo.OrderProjectionMongoRepository;
import vroong.laas.projection.repository.redis.OrderProjectionRedisRepository;

import java.time.Instant;

/**
 * Order Query Service (Reactive)
 * 
 * 역할: Order Projection 조회
 * 전략: Redis → MongoDB → Write Service Fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final OrderProjectionRedisRepository redisRepository;
    private final OrderProjectionMongoRepository mongoRepository;
    private final OrderServiceClient orderServiceClient;
    
    @Value("${projection.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    /**
     * Order Projection 조회 (Reactive)
     * 
     * 순서:
     * 1. Redis 캐시 조회
     * 2. MongoDB 조회 (Cache Miss)
     * 3. Write Service 조회 (Fallback)
     * 
     * @param orderId Order ID
     * @return Mono<OrderProjection>
     */
    public Mono<OrderProjection> getOrderProjection(Long orderId) {
        log.debug("Querying Order Projection: orderId={}", orderId);
        
        // 1. Redis 캐시 조회
        return redisRepository.findByOrderId(orderId)
                .doOnNext(projection -> log.debug("Cache HIT (Redis): orderId={}", orderId))
                // 2. MongoDB 조회 (Redis Miss)
                .switchIfEmpty(Mono.defer(() -> {
                    log.debug("Cache MISS (Redis), trying MongoDB: orderId={}", orderId);
                    return mongoRepository.findByOrderId(orderId)
                            .doOnNext(projection -> {
                                log.debug("Found in MongoDB: orderId={}", orderId);
                                // MongoDB에서 찾았으면 Redis에 캐싱
                                cacheToRedis(orderId, projection);
                            });
                }))
                // 3. Write Service Fallback (MongoDB Miss)
                .switchIfEmpty(Mono.defer(() -> {
                    if (fallbackEnabled) {
                        log.warn("Projection not found, trying fallback: orderId={}", orderId);
                        return fallbackToWriteService(orderId);
                    }
                    return Mono.empty();
                }))
                // 4. 모두 실패하면 에러
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Order not found anywhere: orderId={}", orderId);
                    return Mono.error(new OrderNotFoundException(orderId));
                }));
    }
    
    /**
     * Write Service Fallback (Reactive)
     * 
     * Read Model에 데이터가 없을 때 Write Service의 Query API 호출
     * 
     * @param orderId Order ID
     * @return Mono<OrderProjection>
     */
    private Mono<OrderProjection> fallbackToWriteService(Long orderId) {
        log.info("Fallback to Write Service: orderId={}", orderId);
        
        return orderServiceClient.getOrder(orderId)
                .flatMap(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        OrderProjection projection = convertFromServiceResponse(response.getData());
                        log.info("Fallback success: orderId={}", orderId);
                        // Fallback 데이터를 MongoDB에 저장 (향후 조회 최적화)
                        return saveFallbackData(projection)
                                .thenReturn(projection);
                    }
                    log.warn("Fallback returned empty: orderId={}", orderId);
                    return Mono.empty();
                });
    }
    
    /**
     * Order Service Response → OrderProjection 변환
     */
    private OrderProjection convertFromServiceResponse(OrderServiceResponse.OrderServiceData data) {
        return OrderProjection.builder()
                .orderId(data.getOrderId())
                .orderNumber(data.getOrderNumber())
                .orderStatus("UNKNOWN") // Write Service에서 제공하지 않는 필드
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Fallback 데이터를 MongoDB에 저장 (Reactive)
     * 
     * 목적: 다음 조회 시 Fallback을 거치지 않도록
     */
    private Mono<Void> saveFallbackData(OrderProjection projection) {
        return mongoRepository.save(projection)
                .doOnSuccess(saved -> log.debug("Saved fallback data to MongoDB: orderId={}", projection.getOrderId()))
                .doOnError(e -> log.warn("Failed to save fallback data: orderId={}, error={}", 
                        projection.getOrderId(), e.getMessage()))
                .onErrorResume(e -> Mono.empty())  // 저장 실패는 무시
                .then();
    }
    
    /**
     * MongoDB 결과를 Redis에 캐싱 (Fire and Forget)
     */
    private void cacheToRedis(Long orderId, OrderProjection projection) {
        redisRepository.save(projection)
                .doOnSuccess(saved -> log.debug("Cached to Redis: orderId={}", orderId))
                .doOnError(e -> log.warn("Failed to cache to Redis: orderId={}, error={}", orderId, e.getMessage()))
                .onErrorResume(e -> Mono.empty())  // 캐싱 실패는 무시
                .subscribe();  // Fire and forget
    }
}

