package vroong.laas.readmodel.projection.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vroong.laas.readmodel.common.model.OrderTimelineEvent;
import vroong.laas.readmodel.common.repository.mongo.OrderTimelineDocument;
import vroong.laas.readmodel.common.repository.mongo.OrderTimelineRepository;
import vroong.laas.readmodel.projection.event.OrderEvent;
import vroong.laas.readmodel.projection.event.DispatchEvent;
import vroong.laas.readmodel.projection.event.DeliveryEvent;
import vroong.laas.readmodel.projection.service.ProjectionService;
import vroong.laas.readmodel.common.model.OrderAggregate;
import vroong.laas.common.event.KafkaEventType;
import vroong.laas.common.event.payload.order.OrderLocationEventDto;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderTimelineService {
    
    private final OrderTimelineRepository repository;
    private final ProjectionService projectionService;
    
    /**
     * Order 이벤트를 타임라인으로 변환하여 저장
     */
    public void addOrderTimelineEvent(OrderEvent orderEvent) {
        try {
            KafkaEventType eventType = orderEvent.getKafkaEvent().getType();
            OrderTimelineEvent timelineEvent = null;
            
            if (KafkaEventType.ORDER_ORDER_CREATED.equals(eventType)) {
                timelineEvent = createOrderCreatedTimeline(orderEvent);
            } else if (KafkaEventType.ORDER_ORDER_CANCELLED.equals(eventType)) {
                timelineEvent = createOrderCancelledTimeline(orderEvent);
            } else if (KafkaEventType.ORDER_ORDER_DESTINATION_CHANGED.equals(eventType)) {
                timelineEvent = createDestinationChangedTimeline(orderEvent);
            }
            
            if (timelineEvent != null) {
                OrderTimelineDocument document = OrderTimelineDocument.fromModel(timelineEvent);
                repository.save(document);
                log.debug("Saved order timeline event: orderId={}, eventType={}", 
                        orderEvent.getOrderId(), eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to save order timeline event: orderId={}, error={}", 
                    orderEvent.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * Dispatch 이벤트를 타임라인으로 변환하여 저장
     */
    public void addDispatchTimelineEvent(DispatchEvent dispatchEvent) {
        try {
            KafkaEventType eventType = dispatchEvent.getKafkaEvent().getType();
            OrderTimelineEvent timelineEvent = null;
            
            if (KafkaEventType.DISPATCH_DISPATCH_REQUESTED.equals(eventType)) {
                timelineEvent = createDispatchRequestedTimeline(dispatchEvent);
            } else if (KafkaEventType.DISPATCH_DISPATCH_DISPATCHED.equals(eventType)) {
                timelineEvent = createDispatchDispatchedTimeline(dispatchEvent);
            } else if (KafkaEventType.DISPATCH_DISPATCH_CANCELLED.equals(eventType)) {
                timelineEvent = createDispatchCancelledTimeline(dispatchEvent);
            }
            
            if (timelineEvent != null) {
                OrderTimelineDocument document = OrderTimelineDocument.fromModel(timelineEvent);
                repository.save(document);
                log.debug("Saved dispatch timeline event: orderId={}, eventType={}", 
                        dispatchEvent.getOrderId(), eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to save dispatch timeline event: orderId={}, error={}", 
                    dispatchEvent.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * Delivery 이벤트를 타임라인으로 변환하여 저장
     */
    public void addDeliveryTimelineEvent(DeliveryEvent deliveryEvent) {
        try {
            KafkaEventType eventType = deliveryEvent.getKafkaEvent().getType();
            OrderTimelineEvent timelineEvent = null;
            
            if (KafkaEventType.DELIVERY_DELIVERY_STARTED.equals(eventType)) {
                timelineEvent = createDeliveryStartedTimeline(deliveryEvent);
            } else if (KafkaEventType.DELIVERY_DELIVERY_PICKED_UP.equals(eventType)) {
                timelineEvent = createDeliveryPickedUpTimeline(deliveryEvent);
            } else if (KafkaEventType.DELIVERY_DELIVERY_DELIVERED.equals(eventType)) {
                timelineEvent = createDeliveryDeliveredTimeline(deliveryEvent);
            } else if (KafkaEventType.DELIVERY_DELIVERY_CANCELLED.equals(eventType)) {
                timelineEvent = createDeliveryCancelledTimeline(deliveryEvent);
            }
            
            if (timelineEvent != null) {
                OrderTimelineDocument document = OrderTimelineDocument.fromModel(timelineEvent);
                repository.save(document);
                log.debug("Saved delivery timeline event: orderId={}, eventType={}", 
                        deliveryEvent.getOrderId(), eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to save delivery timeline event: orderId={}, error={}", 
                    deliveryEvent.getOrderId(), e.getMessage(), e);
        }
    }
    
    /**
     * 주문 타임라인 조회
     */
    public List<OrderTimelineEvent> getOrderTimeline(Long orderId) {
        List<OrderTimelineDocument> documents = repository.findByOrderIdOrderByTimestamp(orderId);
        return documents.stream()
                .map(OrderTimelineDocument::toModel)
                .toList();
    }
    
    // === Private Methods for Timeline Creation ===
    
    private OrderTimelineEvent createOrderCreatedTimeline(OrderEvent orderEvent) {
        return OrderTimelineEvent.builder()
                .orderId(orderEvent.getOrderId())
                .timestamp(orderEvent.getOrderedAt())
                .eventType("ORDER_CREATED")
                .description("주문이 생성되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createOrderCancelledTimeline(OrderEvent orderEvent) {
        return OrderTimelineEvent.builder()
                .orderId(orderEvent.getOrderId())
                .timestamp(orderEvent.getCancelledAt())
                .eventType("ORDER_CANCELLED")
                .description("주문이 취소되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDestinationChangedTimeline(OrderEvent orderEvent) {
        // 기존 주문 정보에서 이전 주소 조회
        Optional<OrderAggregate> existingOrder = projectionService.getOrderProjection(orderEvent.getOrderId());
        String beforeAddress = existingOrder
                .map(order -> formatOrderLocation(order.getOrderInfo().getDestinationLocation()))
                .orElse("알 수 없음");
        
        String afterAddress = formatOrderLocationEvent(orderEvent.getDestinationLocation());
        
        OrderTimelineEvent.ChangeInfo changeInfo = OrderTimelineEvent.ChangeInfo.builder()
                .field("destinationAddress")
                .before(beforeAddress)
                .after(afterAddress)
                .build();
        
        return OrderTimelineEvent.builder()
                .orderId(orderEvent.getOrderId())
                .timestamp(orderEvent.getDestinationChangedAt())
                .eventType("DESTINATION_CHANGED")
                .description("배송 주소가 변경되었습니다")
                .changeInfo(changeInfo)
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDispatchRequestedTimeline(DispatchEvent dispatchEvent) {
        return OrderTimelineEvent.builder()
                .orderId(dispatchEvent.getOrderId())
                .timestamp(dispatchEvent.getRequestedAt())
                .eventType("DISPATCH_REQUESTED")
                .description("배차 요청이 시작되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDispatchDispatchedTimeline(DispatchEvent dispatchEvent) {
        return OrderTimelineEvent.builder()
                .orderId(dispatchEvent.getOrderId())
                .timestamp(dispatchEvent.getDispatchedAt())
                .eventType("DISPATCH_DISPATCHED")
                .description("기사님이 배정되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDispatchCancelledTimeline(DispatchEvent dispatchEvent) {
        return OrderTimelineEvent.builder()
                .orderId(dispatchEvent.getOrderId())
                .timestamp(dispatchEvent.getCancelledAt())
                .eventType("DISPATCH_CANCELLED")
                .description("배차가 취소되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDeliveryStartedTimeline(DeliveryEvent deliveryEvent) {
        return OrderTimelineEvent.builder()
                .orderId(deliveryEvent.getOrderId())
                .timestamp(deliveryEvent.getStartedAt())
                .eventType("DELIVERY_STARTED")
                .description("배송이 시작되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDeliveryPickedUpTimeline(DeliveryEvent deliveryEvent) {
        return OrderTimelineEvent.builder()
                .orderId(deliveryEvent.getOrderId())
                .timestamp(deliveryEvent.getPickedUpAt())
                .eventType("DELIVERY_PICKED_UP")
                .description("상품을 픽업했습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDeliveryDeliveredTimeline(DeliveryEvent deliveryEvent) {
        return OrderTimelineEvent.builder()
                .orderId(deliveryEvent.getOrderId())
                .timestamp(deliveryEvent.getDeliveredAt())
                .eventType("DELIVERY_DELIVERED")
                .description("배송이 완료되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private OrderTimelineEvent createDeliveryCancelledTimeline(DeliveryEvent deliveryEvent) {
        return OrderTimelineEvent.builder()
                .orderId(deliveryEvent.getOrderId())
                .timestamp(deliveryEvent.getCancelledAt())
                .eventType("DELIVERY_CANCELLED")
                .description("배송이 취소되었습니다")
                .createdAt(Instant.now())
                .build();
    }
    
    private String formatOrderLocation(OrderAggregate.OrderLocation location) {
        if (location == null) {
            return "알 수 없음";
        }
        
        StringBuilder address = new StringBuilder();
        if (location.getRoadAddress() != null) {
            address.append(location.getRoadAddress());
        } else if (location.getJibunAddress() != null) {
            address.append(location.getJibunAddress());
        }
        
        if (location.getDetailAddress() != null && !location.getDetailAddress().isEmpty()) {
            if (address.length() > 0) {
                address.append(" ");
            }
            address.append(location.getDetailAddress());
        }
        
        return address.length() > 0 ? address.toString() : "알 수 없음";
    }
    
    private String formatOrderLocationEvent(OrderLocationEventDto location) {
        if (location == null) {
            return "알 수 없음";
        }
        
        StringBuilder address = new StringBuilder();
        if (location.getRoadAddress() != null) {
            address.append(location.getRoadAddress());
        } else if (location.getJibunAddress() != null) {
            address.append(location.getJibunAddress());
        }
        
        if (location.getDetailAddress() != null && !location.getDetailAddress().isEmpty()) {
            if (address.length() > 0) {
                address.append(" ");
            }
            address.append(location.getDetailAddress());
        }
        
        return address.length() > 0 ? address.toString() : "알 수 없음";
    }
    
}