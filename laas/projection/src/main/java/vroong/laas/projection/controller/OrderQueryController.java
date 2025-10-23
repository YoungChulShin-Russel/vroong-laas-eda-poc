package vroong.laas.projection.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.projection.dto.ApiResponse;
import vroong.laas.projection.model.projection.OrderProjection;
import vroong.laas.projection.service.OrderQueryService;

/**
 * Order Query Controller (Reactive)
 * 
 * 역할: Order Projection 조회 API 제공
 * 캐싱 전략: Redis → MongoDB → Write Service Fallback
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "projection.features.api.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class OrderQueryController {

    private final OrderQueryService queryService;

    /**
     * Order ID로 Projection 조회 (Reactive)
     * 
     * GET /api/v1/orders/{orderId}
     * 
     * @param orderId Order ID
     * @return Mono<OrderProjection>
     */
    @GetMapping("/{orderId}")
    public Mono<ApiResponse<OrderProjection>> getOrder(@PathVariable Long orderId) {
        log.info("GET /api/v1/orders/{}", orderId);
        
        return queryService.getOrderProjection(orderId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to get order projection: orderId={}, error={}", 
                        orderId, e.getMessage()));
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("OK");
    }
}

