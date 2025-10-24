package vroong.laas.readmodel.query.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.query.client.DeliveryServiceClient;
import vroong.laas.readmodel.query.client.DispatchServiceClient;
import vroong.laas.readmodel.query.client.OrderServiceClient;
import vroong.laas.readmodel.query.client.dto.DeliveryServiceResponse;
import vroong.laas.readmodel.query.client.dto.DispatchServiceResponse;
import vroong.laas.readmodel.query.client.dto.OrderServiceResponse;
import vroong.laas.readmodel.common.exception.OrderNotFoundException;
import vroong.laas.readmodel.common.model.OrderAggregate;
import vroong.laas.readmodel.common.repository.mongo.OrderProjectionMongoRepository;
import vroong.laas.readmodel.common.repository.redis.OrderProjectionRedisRepository;
import vroong.laas.readmodel.query.controller.response.OrderResponse;

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
    
    @Value("${readmodel.fallback.enabled:true}")
    private boolean fallbackEnabled;
    
    /**
     * Order 조회 (API Response용)
     * 
     * @param orderId Order ID
     * @return Mono<OrderResponse>
     */
    public Mono<OrderResponse> getOrder(Long orderId) {
        return getOrderAggregate(orderId)
                .map(OrderResponse::fromOrderAggregate);
    }
    
    /**
     * OrderAggregate 조회 (Internal)
     * 
     * 순서:
     * 1. Redis 캐시 조회
     * 2. MongoDB 조회 (Cache Miss)
     * 3. Write Service 조회 (Fallback)
     * 
     * @param orderId Order ID
     * @return Mono<OrderAggregate>
     */
    private Mono<OrderAggregate> getOrderAggregate(Long orderId) {
        log.debug("Querying Order Projection: orderId={}", orderId);
        
        // 1. Redis 캐시 조회
        return redisRepository.findByOrderId(orderId)
                .doOnNext(__ -> log.debug("Cache HIT (Redis): orderId={}", orderId))
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
    private Mono<OrderAggregate> fallbackToWriteService(Long orderId) {
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
                        OrderAggregate projection = convertFromServiceResponses(
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
     * Order + Dispatch + Delivery Service Response → OrderAggregate 변환
     * 
     * @param orderData Order Service 응답 (필수)
     * @param dispatchData Dispatch Service 응답 (옵셔널)
     * @param deliveryData Delivery Service 응답 (옵셔널)
     * @return OrderAggregate
     */
    private OrderAggregate convertFromServiceResponses(
            OrderServiceResponse.OrderServiceData orderData,
            DispatchServiceResponse.DispatchServiceData dispatchData,
            DeliveryServiceResponse.DeliveryServiceData deliveryData) {
        
        // Order 정보 구성 (필수)
        OrderAggregate.OrderInfo orderInfo = OrderAggregate.OrderInfo.builder()
                .orderNumber(orderData.getOrderNumber())
                .orderStatus("ACTIVE") // 기본값
                .orderedAt(Instant.now())
                // TODO: originLocation, destinationLocation, items는 orderData에서 가져와야 함
                .build();
        
        // Dispatch 정보 구성 (옵셔널)
        OrderAggregate.DispatchInfo dispatchInfo = null;
        if (dispatchData != null) {
            dispatchInfo = OrderAggregate.DispatchInfo.builder()
                    .agentId(dispatchData.getAgentId())
                    .suggestedFee(dispatchData.getDeliveryFee())
                    .requestedAt(null)  // TODO: dispatchData에서 가져와야 함
                    .dispatchedAt(dispatchData.getDispatchedAt())
                    .build();
        }
        
        // Delivery 정보 구성 (옵셔널)
        OrderAggregate.DeliveryInfo deliveryInfo = null;
        if (deliveryData != null) {
            deliveryInfo = OrderAggregate.DeliveryInfo.builder()
                    .deliveryNumber(null)  // TODO: deliveryData에서 가져와야 함
                    .agentId(null)  // TODO: deliveryData에 agentId 추가 필요
                    .deliveryFee(null)  // TODO: deliveryData에서 가져와야 함
                    .deliveryStatus(deliveryData.getDeliveryStatus())
                    .deliveryStartedAt(deliveryData.getDeliveryStartedAt())
                    .deliveryPickedUpAt(deliveryData.getDeliveryPickedUpAt())
                    .deliveryDeliveredAt(deliveryData.getDeliveryDeliveredAt())
                    .deliveryCancelledAt(deliveryData.getDeliveryCancelledAt())
                    .build();
        }
        
        // Aggregate 조립
        return OrderAggregate.builder()
                .orderId(orderData.getOrderId())
                .dispatchId(dispatchData != null ? dispatchData.getDispatchId() : null)
                .deliveryId(deliveryData != null ? deliveryData.getDeliveryId() : null)
                .orderInfo(orderInfo)
                .dispatchInfo(dispatchInfo)
                .deliveryInfo(deliveryInfo)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Fallback 데이터를 MongoDB에 저장 (Reactive)
     * 
     * 목적: 다음 조회 시 Fallback을 거치지 않도록
     */
    private Mono<Void> saveFallbackData(OrderAggregate projection) {
        return mongoRepository.save(projection)
                .doOnSuccess(__ -> log.debug("Saved fallback data to MongoDB: orderId={}", projection.getOrderId()))
                .doOnError(__ -> log.warn("Failed to save fallback data: orderId={}", projection.getOrderId()))
                .onErrorResume(__ -> Mono.empty())  // 저장 실패는 무시
                .then();
    }
    
    /**
     * MongoDB 결과를 Redis에 캐싱 (Fire and Forget)
     */
    private void cacheToRedis(Long orderId, OrderAggregate projection) {
        redisRepository.save(projection)
                .doOnSuccess(__ -> log.debug("Cached to Redis: orderId={}", orderId))
                .doOnError(__ -> log.warn("Failed to cache to Redis: orderId={}", orderId))
                .onErrorResume(__ -> Mono.empty())  // 캐싱 실패는 무시
                .subscribe();  // Fire and forget
    }
}

