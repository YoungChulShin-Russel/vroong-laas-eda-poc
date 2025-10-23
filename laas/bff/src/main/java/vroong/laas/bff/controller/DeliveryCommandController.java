package vroong.laas.bff.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.bff.dto.ApiResponse;
import vroong.laas.bff.service.DeliveryCommandService;

import java.util.Map;

/**
 * Delivery Command Controller
 * 배송 관련 명령 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryCommandController {

    private final DeliveryCommandService deliveryCommandService;

    /**
     * 기사 상점 도착 (픽업)
     * POST /api/v1/commands/deliveries/{deliveryId}/pickup
     */
    @PostMapping("/{deliveryId}/pickup")
    public Mono<ApiResponse<Map<String, Object>>> pickupAtStore(@PathVariable Long deliveryId) {
        log.info("POST /api/v1/commands/deliveries/{}/pickup", deliveryId);
        return deliveryCommandService.pickupAtStore(deliveryId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to pickup at store: deliveryId={}, error={}", 
                        deliveryId, e.getMessage()));
    }

    /**
     * 기사 배송 완료
     * POST /api/v1/commands/deliveries/{deliveryId}/deliver
     */
    @PostMapping("/{deliveryId}/deliver")
    public Mono<ApiResponse<Map<String, Object>>> completeDelivery(@PathVariable Long deliveryId) {
        log.info("POST /api/v1/commands/deliveries/{}/deliver", deliveryId);
        return deliveryCommandService.completeDelivery(deliveryId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to complete delivery: deliveryId={}, error={}", 
                        deliveryId, e.getMessage()));
    }
}

