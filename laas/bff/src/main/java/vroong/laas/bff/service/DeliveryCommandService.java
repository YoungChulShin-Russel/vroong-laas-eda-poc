package vroong.laas.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.bff.client.DeliveryServiceClient;

import java.util.Map;

/**
 * Delivery Command Service
 * 배송 관련 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryCommandService {

    private final DeliveryServiceClient deliveryServiceClient;

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

