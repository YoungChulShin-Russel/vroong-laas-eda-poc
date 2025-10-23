package vroong.laas.bff.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.bff.dto.ApiResponse;
import vroong.laas.bff.service.CommandService;

import java.util.Map;

/**
 * Command Controller
 * MSA 서비스로 명령을 전달하는 BFF 엔드포인트
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/commands")
@RequiredArgsConstructor
public class CommandController {

    private final CommandService commandService;

    // ==================== Order Commands ====================

    /**
     * 주문 생성
     * POST /api/v1/commands/orders
     */
    @PostMapping("/orders")
    public Mono<ApiResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        log.info("POST /api/v1/commands/orders");
        return commandService.createOrder(orderRequest)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to create order: {}", e.getMessage()));
    }

    // ==================== Dispatch Commands ====================

    /**
     * 기사 배차 제안 수락
     * POST /api/v1/commands/dispatches/response
     */
    @PostMapping("/dispatches/response")
    public Mono<ApiResponse<Map<String, Object>>> acceptDispatch(@RequestBody Map<String, Object> responseRequest) {
        log.info("POST /api/v1/commands/dispatches/response");
        return commandService.acceptDispatch(responseRequest)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to accept dispatch: {}", e.getMessage()));
    }

    // ==================== Delivery Commands ====================

    /**
     * 기사 상점 도착 (픽업)
     * POST /api/v1/commands/deliveries/{deliveryId}/pickup
     */
    @PostMapping("/deliveries/{deliveryId}/pickup")
    public Mono<ApiResponse<Map<String, Object>>> pickupAtStore(@PathVariable Long deliveryId) {
        log.info("POST /api/v1/commands/deliveries/{}/pickup", deliveryId);
        return commandService.pickupAtStore(deliveryId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to pickup at store: deliveryId={}, error={}", 
                        deliveryId, e.getMessage()));
    }

    /**
     * 기사 배송 완료
     * POST /api/v1/commands/deliveries/{deliveryId}/deliver
     */
    @PostMapping("/deliveries/{deliveryId}/deliver")
    public Mono<ApiResponse<Map<String, Object>>> completeDelivery(@PathVariable Long deliveryId) {
        log.info("POST /api/v1/commands/deliveries/{}/deliver", deliveryId);
        return commandService.completeDelivery(deliveryId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to complete delivery: deliveryId={}, error={}", 
                        deliveryId, e.getMessage()));
    }
}

