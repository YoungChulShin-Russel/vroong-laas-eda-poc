package vroong.laas.projection.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import vroong.laas.projection.model.projection.OrderProjection;
import vroong.laas.projection.model.redis.OrderRedisModel;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderProjectionRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration redisTtl;

    public void save(OrderProjection projection) {
        String key = OrderRedisModel.generateKey(projection.getOrderId());
        OrderRedisModel redisModel = OrderRedisModel.from(projection);
        
        try {
            redisTemplate.opsForValue().set(key, redisModel, redisTtl);
            log.debug("Saved order projection to Redis: orderId={}, key={}", 
                    projection.getOrderId(), key);
        } catch (Exception e) {
            log.error("Failed to save order projection to Redis: orderId={}, error={}", 
                    projection.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save to Redis", e);
        }
    }

    public Optional<OrderProjection> findByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        try {
            Object result = redisTemplate.opsForValue().get(key);
            if (result instanceof OrderRedisModel redisModel) {
                OrderProjection projection = redisModel.toProjection();
                log.debug("Found order projection in Redis: orderId={}", orderId);
                return Optional.of(projection);
            }
            
            log.debug("Order projection not found in Redis: orderId={}", orderId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to find order projection in Redis: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void deleteByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Deleted order projection from Redis: orderId={}", orderId);
            } else {
                log.debug("Order projection not found for deletion in Redis: orderId={}", orderId);
            }
        } catch (Exception e) {
            log.error("Failed to delete order projection from Redis: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
        }
    }

    public boolean existsByOrderId(Long orderId) {
        String key = OrderRedisModel.generateKey(orderId);
        
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.error("Failed to check existence in Redis: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return false;
        }
    }
}