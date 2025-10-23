package vroong.laas.readmodel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vroong.laas.readmodel.model.projection.OrderProjection;
import vroong.laas.readmodel.repository.mongo.OrderProjectionMongoRepository;
import vroong.laas.readmodel.repository.redis.OrderProjectionRedisRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionService {

    private final OrderProjectionRedisRepository redisRepository;
    private final OrderProjectionMongoRepository mongoRepository;

    /**
     * OrderProjection을 Redis와 MongoDB에 저장합니다.
     * Redis는 1일 TTL로 캐시 역할을 하고, MongoDB는 영구 저장소 역할을 합니다.
     */
    public void saveOrderProjection(OrderProjection projection) {
        log.debug("Saving order projection: orderId={}", projection.getOrderId());
        
        try {
            // Redis에 저장 (1일 TTL, Reactive → Blocking)
            redisRepository.save(projection).block();
            
            // MongoDB에 저장 (영구 보관, Reactive → Blocking)
            mongoRepository.save(projection).block();
            
            log.info("Successfully saved order projection: orderId={}", projection.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to save order projection: orderId={}, error={}", 
                    projection.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save order projection", e);
        }
    }

    /**
     * 캐시 패턴으로 OrderProjection을 조회합니다.
     * 1. Redis에서 먼저 조회
     * 2. Redis에 없으면 MongoDB에서 조회
     * 3. MongoDB에서 찾으면 Redis에 캐시
     */
    public Optional<OrderProjection> getOrderProjection(Long orderId) {
        log.debug("Getting order projection: orderId={}", orderId);
        
        try {
            // 1. Redis에서 먼저 조회 (Reactive → Blocking)
            OrderProjection redisResult = redisRepository.findByOrderId(orderId).block();
            if (redisResult != null) {
                log.debug("Found order projection in Redis: orderId={}", orderId);
                return Optional.of(redisResult);
            }
            
            // 2. Redis에 없으면 MongoDB에서 조회 (Reactive → Blocking)
            OrderProjection mongoResult = mongoRepository.findByOrderId(orderId).block();
            if (mongoResult != null) {
                log.debug("Found order projection in MongoDB, caching to Redis: orderId={}", orderId);
                
                // 3. MongoDB에서 찾으면 Redis에 캐시
                try {
                    redisRepository.save(mongoResult).block();
                } catch (Exception e) {
                    log.warn("Failed to cache projection to Redis: orderId={}, error={}", 
                            orderId, e.getMessage());
                    // Redis 캐싱 실패는 무시하고 MongoDB 결과 반환
                }
                
                return Optional.of(mongoResult);
            }
            
            log.debug("Order projection not found: orderId={}", orderId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to get order projection: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * OrderProjection을 업데이트합니다.
     * 기존 projection을 조회한 후 새로운 정보로 업데이트하여 저장합니다.
     */
    public Optional<OrderProjection> updateOrderProjection(Long orderId, 
                                                          java.util.function.Function<OrderProjection, OrderProjection> updateFunction) {
        log.debug("Updating order projection: orderId={}", orderId);
        
        try {
            Optional<OrderProjection> existingProjection = getOrderProjection(orderId);
            if (existingProjection.isEmpty()) {
                log.warn("Cannot update non-existent order projection: orderId={}", orderId);
                return Optional.empty();
            }
            
            OrderProjection updatedProjection = updateFunction.apply(existingProjection.get());
            saveOrderProjection(updatedProjection);
            
            log.info("Successfully updated order projection: orderId={}", orderId);
            return Optional.of(updatedProjection);
            
        } catch (Exception e) {
            log.error("Failed to update order projection: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * OrderProjection을 삭제합니다.
     * Redis와 MongoDB에서 모두 삭제합니다.
     */
    public void deleteOrderProjection(Long orderId) {
        log.debug("Deleting order projection: orderId={}", orderId);
        
        try {
            redisRepository.deleteByOrderId(orderId);
            mongoRepository.deleteByOrderId(orderId);
            
            log.info("Successfully deleted order projection: orderId={}", orderId);
            
        } catch (Exception e) {
            log.error("Failed to delete order projection: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
        }
    }

    /**
     * OrderProjection의 존재 여부를 확인합니다.
     * Redis 또는 MongoDB 중 하나라도 존재하면 true를 반환합니다.
     */
    public boolean existsOrderProjection(Long orderId) {
        try {
            Boolean redisExists = redisRepository.existsByOrderId(orderId).block();
            Boolean mongoExists = mongoRepository.existsByOrderId(orderId).block();
            return Boolean.TRUE.equals(redisExists) || Boolean.TRUE.equals(mongoExists);
        } catch (Exception e) {
            log.error("Failed to check order projection existence: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return false;
        }
    }
}