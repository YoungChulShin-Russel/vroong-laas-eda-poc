package vroong.laas.readmodel.projection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vroong.laas.readmodel.projection.handler.DeliveryProjectionHandler;
import vroong.laas.readmodel.projection.handler.DispatchProjectionHandler;
import vroong.laas.readmodel.projection.handler.OrderProjectionHandler;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.projection.event.DispatchEvent;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.common.model.OrderAggregate;

import java.util.Optional;

/**
 * 이벤트별 projection 처리를 조율하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectionOrchestrator {

    private final OrderProjectionHandler orderProjectionHandler;
    private final DeliveryProjectionHandler deliveryProjectionHandler;  
    private final DispatchProjectionHandler dispatchProjectionHandler;
    private final ProjectionService projectionService;

    /**
     * Order 이벤트 처리 (생성, 수정, 취소 등)
     */
    public void handleOrderEvent(OrderEvent orderEvent) {
        log.debug("Orchestrating order event: orderId={}, eventType={}", 
                orderEvent.getOrderId(), orderEvent.getKafkaEvent().getType());
        
        try {
            // 1. Order projection 처리 (생성/수정/취소 등)
            OrderAggregate projection = orderProjectionHandler.handleOrderEvent(orderEvent);
            
            // 2. Redis + MongoDB에 저장
            projectionService.saveOrderProjection(projection);
            
            log.info("Successfully processed order event: orderId={}, eventType={}", 
                    orderEvent.getOrderId(), orderEvent.getKafkaEvent().getType());
            
        } catch (Exception e) {
            log.error("Failed to process order event: orderId={}, eventType={}, error={}", 
                    orderEvent.getOrderId(), orderEvent.getKafkaEvent().getType(), e.getMessage(), e);
            throw new RuntimeException("Failed to process order event", e);
        }
    }

    /**
     * Dispatch 이벤트 처리
     */
    public void handleDispatchEvent(DispatchEvent dispatchEvent) {
        log.debug("Orchestrating dispatch event: dispatchId={}, orderId={}", 
                dispatchEvent.getDispatchId(), dispatchEvent.getOrderId());
        
        try {
            // 1. 기존 projection 조회
            Optional<OrderAggregate> existingProjection =
                    projectionService.getOrderProjection(dispatchEvent.getOrderId());
            
            if (existingProjection.isEmpty()) {
                log.warn("Order projection not found for dispatch event: orderId={}, dispatchId={}", 
                        dispatchEvent.getOrderId(), dispatchEvent.getDispatchId());
                return;
            }
            
            // 2. Dispatch 정보 업데이트
            OrderAggregate updatedProjection = dispatchProjectionHandler.updateDispatchInfo(
                    existingProjection.get(), dispatchEvent);
            
            // 3. 저장
            projectionService.saveOrderProjection(updatedProjection);
            
            
            log.info("Successfully processed dispatch event: dispatchId={}, orderId={}", 
                    dispatchEvent.getDispatchId(), dispatchEvent.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to process dispatch event: dispatchId={}, orderId={}, error={}", 
                    dispatchEvent.getDispatchId(), dispatchEvent.getOrderId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process dispatch event", e);
        }
    }

    /**
     * Delivery 이벤트 처리
     */
    public void handleDeliveryEvent(DeliveryEvent deliveryEvent) {
        log.debug("Orchestrating delivery event: deliveryId={}, agentId={}, eventType={}", 
                deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), deliveryEvent.getEventType());
        
        try {
            // 1. delivery event에서 직접 orderId 사용
            Long orderId = deliveryEvent.getOrderId();
            
            if (orderId == null) {
                log.warn("Order ID not found in delivery event: deliveryId={}, agentId={}", 
                        deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId());
                return;
            }
            
            // 2. 기존 projection 조회
            Optional<OrderAggregate> existingProjection = projectionService.getOrderProjection(orderId);
            
            if (existingProjection.isEmpty()) {
                log.warn("Order projection not found for delivery event: orderId={}, deliveryId={}", 
                        orderId, deliveryEvent.getDeliveryId());
                return;
            }
            
            // 3. Delivery 상태 업데이트
            OrderAggregate updatedProjection = deliveryProjectionHandler.updateDeliveryStatus(
                    existingProjection.get(), deliveryEvent);
            
            // 4. 저장
            projectionService.saveOrderProjection(updatedProjection);
            
            
            log.info("Successfully processed delivery event: deliveryId={}, orderId={}, eventType={}", 
                    deliveryEvent.getDeliveryId(), orderId, deliveryEvent.getEventType());
            
        } catch (Exception e) {
            log.error("Failed to process delivery event: deliveryId={}, agentId={}, error={}", 
                    deliveryEvent.getDeliveryId(), deliveryEvent.getAgentId(), e.getMessage(), e);
            throw new RuntimeException("Failed to process delivery event", e);
        }
    }
}