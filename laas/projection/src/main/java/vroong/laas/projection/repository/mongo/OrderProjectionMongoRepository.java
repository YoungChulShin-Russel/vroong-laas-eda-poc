package vroong.laas.projection.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import vroong.laas.projection.model.document.OrderDocument;
import vroong.laas.projection.model.projection.OrderProjection;

import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderProjectionMongoRepository {

    private final MongoTemplate mongoTemplate;

    public void save(OrderProjection projection) {
        OrderDocument document = OrderDocument.from(projection);
        
        try {
            OrderDocument saved = mongoTemplate.save(document);
            log.debug("Saved order projection to MongoDB: orderId={}, id={}", 
                    projection.getOrderId(), saved.getId());
        } catch (Exception e) {
            log.error("Failed to save order projection to MongoDB: orderId={}, error={}", 
                    projection.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save to MongoDB", e);
        }
    }

    public Optional<OrderProjection> findByOrderId(Long orderId) {
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            OrderDocument document = mongoTemplate.findOne(query, OrderDocument.class);
            
            if (document != null) {
                OrderProjection projection = document.toProjection();
                log.debug("Found order projection in MongoDB: orderId={}", orderId);
                return Optional.of(projection);
            }
            
            log.debug("Order projection not found in MongoDB: orderId={}", orderId);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Failed to find order projection in MongoDB: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return Optional.empty();
        }
    }


    public void deleteByOrderId(Long orderId) {
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            mongoTemplate.remove(query, OrderDocument.class);
            log.debug("Deleted order projection from MongoDB: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Failed to delete order projection from MongoDB: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
        }
    }

    public boolean existsByOrderId(Long orderId) {
        try {
            Query query = new Query(Criteria.where("orderId").is(orderId));
            return mongoTemplate.exists(query, OrderDocument.class);
        } catch (Exception e) {
            log.error("Failed to check existence in MongoDB: orderId={}, error={}", 
                    orderId, e.getMessage(), e);
            return false;
        }
    }

}