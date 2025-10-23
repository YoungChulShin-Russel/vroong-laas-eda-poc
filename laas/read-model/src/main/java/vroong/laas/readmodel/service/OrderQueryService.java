package vroong.laas.readmodel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.client.DeliveryServiceClient;
import vroong.laas.readmodel.client.DispatchServiceClient;
import vroong.laas.readmodel.client.OrderServiceClient;
import vroong.laas.readmodel.dto.DeliveryServiceResponse;
import vroong.laas.readmodel.dto.DispatchServiceResponse;
import vroong.laas.readmodel.dto.OrderServiceResponse;
import vroong.laas.readmodel.exception.OrderNotFoundException;
import vroong.laas.readmodel.model.projection.OrderProjection;
import vroong.laas.readmodel.repository.mongo.OrderProjectionMongoRepository;
import vroong.laas.readmodel.repository.redis.OrderProjectionRedisRepository;

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
    private final DispatchServiceClient dispatchServiceClient;
    private final DeliveryServiceClient deliveryServiceClient;
    
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
     * Read Model에 데이터가 없을 때 Order, Dispatch, Delivery Service를 모두 호출하여 조합
     * 
     * @param orderId Order ID
     * @return Mono<OrderProjection>
     */
    private Mono<OrderProjection> fallbackToWriteService(Long orderId) {
        log.info("Fallback to Write Services (Order + Dispatch + Delivery): orderId={}", orderId);
        
        // 세 서비스를 병렬로 호출
        Mono<OrderServiceResponse.OrderServiceData> orderMono = orderServiceClient.getOrder(orderId)
                .filter(response -> response.isSuccess() && response.getData() != null)
                .map(OrderServiceResponse::getData);
        
        Mono<DispatchServiceResponse.DispatchServiceData> dispatchMono = dispatchServiceClient.getDispatchByOrderId(orderId)
                .filter(response -> response.isSuccess() && response.getData() != null)
                .map(DispatchServiceResponse::getData);
        
        Mono<DeliveryServiceResponse.DeliveryServiceData> deliveryMono = deliveryServiceClient.getDeliveryByOrderId(orderId)
                .filter(response -> response.isSuccess() && response.getData() != null)
                .map(DeliveryServiceResponse::getData);
        
        // 세 결과를 조합 (Order는 필수, Dispatch/Delivery는 옵셔널)
        return orderMono
                .flatMap(orderData -> 
                    Mono.zip(
                        Mono.just(orderData),
                        dispatchMono.defaultIfEmpty(null),
                        deliveryMono.defaultIfEmpty(null)
                    )
                    .map(tuple -> {
                        OrderProjection projection = convertFromServiceResponses(
                            tuple.getT1(),  // Order
                            tuple.getT2(),  // Dispatch (nullable)
                            tuple.getT3()   // Delivery (nullable)
                        );
                        log.info("Fallback success (combined): orderId={}", orderId);
                        return projection;
                    })
                )
                .flatMap(projection -> 
                    // Fallback 데이터를 MongoDB에 저장 (향후 조회 최적화)
                    saveFallbackData(projection)
                        .thenReturn(projection)
                )
                .doOnError(e -> log.error("Fallback failed: orderId={}, error={}", orderId, e.getMessage()));
    }
    
    /**
     * Order + Dispatch + Delivery Service Response → OrderProjection 변환
     * 
     * @param orderData Order Service 응답 (필수)
     * @param dispatchData Dispatch Service 응답 (옵셔널)
     * @param deliveryData Delivery Service 응답 (옵셔널)
     * @return OrderProjection
     */
    private OrderProjection convertFromServiceResponses(
            OrderServiceResponse.OrderServiceData orderData,
            DispatchServiceResponse.DispatchServiceData dispatchData,
            DeliveryServiceResponse.DeliveryServiceData deliveryData) {
        
        OrderProjection.OrderProjectionBuilder builder = OrderProjection.builder()
                .orderId(orderData.getOrderId())
                .orderNumber(orderData.getOrderNumber())
                .orderStatus("ACTIVE") // 기본값
                .createdAt(Instant.now())
                .updatedAt(Instant.now());
        
        // Dispatch 정보 추가
        if (dispatchData != null) {
            builder
                .dispatchId(dispatchData.getDispatchId())
                .agentId(dispatchData.getAgentId())
                .deliveryFee(dispatchData.getDeliveryFee())
                .dispatchedAt(dispatchData.getDispatchedAt());
        }
        
        // Delivery 정보 추가
        if (deliveryData != null) {
            builder
                .deliveryId(deliveryData.getDeliveryId())
                .deliveryStatus(deliveryData.getDeliveryStatus())
                .deliveryStartedAt(deliveryData.getDeliveryStartedAt())
                .deliveryPickedUpAt(deliveryData.getDeliveryPickedUpAt())
                .deliveryDeliveredAt(deliveryData.getDeliveryDeliveredAt())
                .deliveryCancelledAt(deliveryData.getDeliveryCancelledAt());
        }
        
        return builder.build();
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

