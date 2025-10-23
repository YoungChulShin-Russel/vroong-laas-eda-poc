package vroong.laas.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.bff.client.DeliveryServiceClient;
import vroong.laas.bff.client.DispatchServiceClient;
import vroong.laas.bff.client.OrderServiceClient;

import java.util.Map;

/**
 * Command Service
 * MSA 서비스로 명령을 라우팅
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {

    private final OrderServiceClient orderServiceClient;
    private final DispatchServiceClient dispatchServiceClient;
    private final DeliveryServiceClient deliveryServiceClient;

    // ==================== Order Commands ====================

    /**
     * 주문 생성
     * POST /api/v1/orders
     */
    public Mono<Map<String, Object>> createOrder(Map<String, Object> orderRequest) {
        log.info("Creating order");
        return orderServiceClient.createOrder(orderRequest);
    }

    // ==================== Dispatch Commands ====================

    /**
     * 기사 배차 제안 수락
     * POST /api/v1/dispatches/response
     */
    public Mono<Map<String, Object>> acceptDispatch(Map<String, Object> responseRequest) {
        log.info("Accepting dispatch");
        return dispatchServiceClient.acceptDispatch(responseRequest);
    }

    // ==================== Delivery Commands ====================

    /**
     * 기사 상점 도착 (픽업)
     * POST /api/v1/deliveries/{deliveryId}/pickup
     */
    public Mono<Map<String, Object>> pickupAtStore(Long deliveryId) {
        log.info("Pickup at store: deliveryId={}", deliveryId);
        return deliveryServiceClient.pickupAtStore(deliveryId);
    }

    /**
     * 기사 배송 완료
     * POST /api/v1/deliveries/{deliveryId}/deliver
     */
    public Mono<Map<String, Object>> completeDelivery(Long deliveryId) {
        log.info("Completing delivery: deliveryId={}", deliveryId);
        return deliveryServiceClient.completeDelivery(deliveryId);
    }
}

