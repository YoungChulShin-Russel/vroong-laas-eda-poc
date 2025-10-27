package vroong.laas.readmodel.common.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.time.Duration;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

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
    public Mono<OrderAggregate> save(OrderAggregate projection) {
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
    public Mono<OrderAggregate> findByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        return reactiveRedisTemplate.opsForValue()
                .get(key)
                .flatMap(this::convertToOrderRedisModel)
                .map(OrderRedisModel::toAggregate)
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

    /**
     * Redis에서 가져온 Object를 OrderRedisModel로 안전하게 변환
     */
    private Mono<OrderRedisModel> convertToOrderRedisModel(Object redisData) {
        try {
            if (redisData instanceof OrderRedisModel) {
                return Mono.just((OrderRedisModel) redisData);
            }
            
            if (redisData instanceof LinkedHashMap) {
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) redisData;
                OrderRedisModel converted = convertMapToOrderRedisModel(map);
                return Mono.just(converted);
            }
            
            log.warn("Unexpected redis data type: {}", redisData.getClass().getSimpleName());
            return Mono.empty();
            
        } catch (Exception e) {
            log.error("Failed to convert redis data to OrderRedisModel: {}, dataType: {}", 
                    e.getMessage(), redisData != null ? redisData.getClass().getSimpleName() : "null");
            return Mono.empty();
        }
    }

    /**
     * LinkedHashMap을 OrderRedisModel로 수동 변환
     */
    private OrderRedisModel convertMapToOrderRedisModel(LinkedHashMap<String, Object> map) {
        OrderRedisModel.OrderRedisModelBuilder builder = OrderRedisModel.builder();
        
        // Root level fields (snake_case)
        if (map.get("order_id") != null) {
            builder.orderId(((Number) map.get("order_id")).longValue());
        }
        if (map.get("dispatch_id") != null) {
            builder.dispatchId(((Number) map.get("dispatch_id")).longValue());
        }
        if (map.get("delivery_id") != null) {
            builder.deliveryId(((Number) map.get("delivery_id")).longValue());
        }
        if (map.get("created_at") != null) {
            builder.createdAt(Instant.parse((String) map.get("created_at")));
        }
        if (map.get("updated_at") != null) {
            builder.updatedAt(Instant.parse((String) map.get("updated_at")));
        }
        
        // Nested objects (snake_case)
        if (map.get("order_info") instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> orderInfoMap = (LinkedHashMap<String, Object>) map.get("order_info");
            builder.orderInfo(convertToOrderInfoRedis(orderInfoMap));
        }
        
        if (map.get("dispatch_info") instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> dispatchInfoMap = (LinkedHashMap<String, Object>) map.get("dispatch_info");
            builder.dispatchInfo(convertToDispatchInfoRedis(dispatchInfoMap));
        }
        
        if (map.get("delivery_info") instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> deliveryInfoMap = (LinkedHashMap<String, Object>) map.get("delivery_info");
            builder.deliveryInfo(convertToDeliveryInfoRedis(deliveryInfoMap));
        }
        
        return builder.build();
    }
    
    private OrderRedisModel.OrderInfoRedis convertToOrderInfoRedis(LinkedHashMap<String, Object> map) {
        OrderRedisModel.OrderInfoRedis.OrderInfoRedisBuilder builder = OrderRedisModel.OrderInfoRedis.builder();
        
        if (map.get("order_number") != null) {
            builder.orderNumber((String) map.get("order_number"));
        }
        if (map.get("order_status") != null) {
            builder.orderStatus((String) map.get("order_status"));
        }
        if (map.get("ordered_at") != null) {
            builder.orderedAt(Instant.parse((String) map.get("ordered_at")));
        }
        
        if (map.get("origin_location") instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> locationMap = (LinkedHashMap<String, Object>) map.get("origin_location");
            builder.originLocation(convertToOrderLocationRedis(locationMap));
        }
        
        if (map.get("destination_location") instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> locationMap = (LinkedHashMap<String, Object>) map.get("destination_location");
            builder.destinationLocation(convertToOrderLocationRedis(locationMap));
        }
        
        if (map.get("items") instanceof List) {
            @SuppressWarnings("unchecked")
            List<LinkedHashMap<String, Object>> itemsList = (List<LinkedHashMap<String, Object>>) map.get("items");
            List<OrderRedisModel.OrderItemRedis> items = new ArrayList<>();
            for (LinkedHashMap<String, Object> itemMap : itemsList) {
                items.add(convertToOrderItemRedis(itemMap));
            }
            builder.items(items);
        }
        
        return builder.build();
    }
    
    private OrderRedisModel.DispatchInfoRedis convertToDispatchInfoRedis(LinkedHashMap<String, Object> map) {
        OrderRedisModel.DispatchInfoRedis.DispatchInfoRedisBuilder builder = OrderRedisModel.DispatchInfoRedis.builder();
        
        if (map.get("agent_id") != null) {
            builder.agentId(((Number) map.get("agent_id")).longValue());
        }
        if (map.get("suggested_fee") != null) {
            builder.suggestedFee(new BigDecimal(map.get("suggested_fee").toString()));
        }
        if (map.get("requested_at") != null) {
            builder.requestedAt(Instant.parse((String) map.get("requested_at")));
        }
        if (map.get("dispatched_at") != null) {
            builder.dispatchedAt(Instant.parse((String) map.get("dispatched_at")));
        }
        
        return builder.build();
    }
    
    private OrderRedisModel.DeliveryInfoRedis convertToDeliveryInfoRedis(LinkedHashMap<String, Object> map) {
        OrderRedisModel.DeliveryInfoRedis.DeliveryInfoRedisBuilder builder = OrderRedisModel.DeliveryInfoRedis.builder();
        
        if (map.get("delivery_number") != null) {
            builder.deliveryNumber((String) map.get("delivery_number"));
        }
        if (map.get("agent_id") != null) {
            builder.agentId(((Number) map.get("agent_id")).longValue());
        }
        if (map.get("delivery_fee") != null) {
            builder.deliveryFee(new BigDecimal(map.get("delivery_fee").toString()));
        }
        if (map.get("delivery_status") != null) {
            builder.deliveryStatus((String) map.get("delivery_status"));
        }
        if (map.get("delivery_started_at") != null) {
            builder.deliveryStartedAt(Instant.parse((String) map.get("delivery_started_at")));
        }
        if (map.get("delivery_picked_up_at") != null) {
            builder.deliveryPickedUpAt(Instant.parse((String) map.get("delivery_picked_up_at")));
        }
        if (map.get("delivery_delivered_at") != null) {
            builder.deliveryDeliveredAt(Instant.parse((String) map.get("delivery_delivered_at")));
        }
        if (map.get("delivery_cancelled_at") != null) {
            builder.deliveryCancelledAt(Instant.parse((String) map.get("delivery_cancelled_at")));
        }
        
        return builder.build();
    }
    
    private OrderRedisModel.OrderLocationRedis convertToOrderLocationRedis(LinkedHashMap<String, Object> map) {
        OrderRedisModel.OrderLocationRedis.OrderLocationRedisBuilder builder = OrderRedisModel.OrderLocationRedis.builder();
        
        if (map.get("contact_name") != null) {
            builder.contactName((String) map.get("contact_name"));
        }
        if (map.get("contact_phone_number") != null) {
            builder.contactPhoneNumber((String) map.get("contact_phone_number"));
        }
        if (map.get("latitude") != null) {
            builder.latitude(new BigDecimal(map.get("latitude").toString()));
        }
        if (map.get("longitude") != null) {
            builder.longitude(new BigDecimal(map.get("longitude").toString()));
        }
        if (map.get("jibun_address") != null) {
            builder.jibunAddress((String) map.get("jibun_address"));
        }
        if (map.get("road_address") != null) {
            builder.roadAddress((String) map.get("road_address"));
        }
        if (map.get("detail_address") != null) {
            builder.detailAddress((String) map.get("detail_address"));
        }
        
        return builder.build();
    }
    
    private OrderRedisModel.OrderItemRedis convertToOrderItemRedis(LinkedHashMap<String, Object> map) {
        OrderRedisModel.OrderItemRedis.OrderItemRedisBuilder builder = OrderRedisModel.OrderItemRedis.builder();
        
        if (map.get("item_name") != null) {
            builder.itemName((String) map.get("item_name"));
        }
        if (map.get("quantity") != null) {
            builder.quantity(((Number) map.get("quantity")).intValue());
        }
        if (map.get("price") != null) {
            builder.price(new BigDecimal(map.get("price").toString()));
        }
        
        return builder.build();
    }
}