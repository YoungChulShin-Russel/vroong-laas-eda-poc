package vroong.laas.projection.repository.mongo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import vroong.laas.projection.model.document.OrderDocument;
import vroong.laas.projection.model.projection.OrderProjection;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<OrderProjection> findByAgentId(Long agentId) {
        try {
            Query query = new Query(Criteria.where("agentId").is(agentId));
            List<OrderDocument> documents = mongoTemplate.find(query, OrderDocument.class);
            
            List<OrderProjection> projections = documents.stream()
                    .map(OrderDocument::toProjection)
                    .collect(Collectors.toList());
            
            log.debug("Found {} order projections for agentId={}", projections.size(), agentId);
            return projections;
            
        } catch (Exception e) {
            log.error("Failed to find order projections by agentId: agentId={}, error={}", 
                    agentId, e.getMessage(), e);
            return List.of();
        }
    }

    public List<OrderProjection> findByOrderedAtBetween(Instant startTime, Instant endTime) {
        try {
            Query query = new Query(
                    Criteria.where("orderedAt")
                            .gte(startTime)
                            .lte(endTime)
            );
            List<OrderDocument> documents = mongoTemplate.find(query, OrderDocument.class);
            
            List<OrderProjection> projections = documents.stream()
                    .map(OrderDocument::toProjection)
                    .collect(Collectors.toList());
            
            log.debug("Found {} order projections between {} and {}", 
                    projections.size(), startTime, endTime);
            return projections;
            
        } catch (Exception e) {
            log.error("Failed to find order projections by date range: startTime={}, endTime={}, error={}", 
                    startTime, endTime, e.getMessage(), e);
            return List.of();
        }
    }

    public List<OrderProjection> findByDeliveryStatus(String deliveryStatus) {
        try {
            Query query = new Query(Criteria.where("deliveryStatus").is(deliveryStatus));
            List<OrderDocument> documents = mongoTemplate.find(query, OrderDocument.class);
            
            List<OrderProjection> projections = documents.stream()
                    .map(OrderDocument::toProjection)
                    .collect(Collectors.toList());
            
            log.debug("Found {} order projections with deliveryStatus={}", 
                    projections.size(), deliveryStatus);
            return projections;
            
        } catch (Exception e) {
            log.error("Failed to find order projections by delivery status: deliveryStatus={}, error={}", 
                    deliveryStatus, e.getMessage(), e);
            return List.of();
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

    public long countByDeliveryStatus(String deliveryStatus) {
        try {
            Query query = new Query(Criteria.where("deliveryStatus").is(deliveryStatus));
            return mongoTemplate.count(query, OrderDocument.class);
        } catch (Exception e) {
            log.error("Failed to count by delivery status: deliveryStatus={}, error={}", 
                    deliveryStatus, e.getMessage(), e);
            return 0;
        }
    }
}