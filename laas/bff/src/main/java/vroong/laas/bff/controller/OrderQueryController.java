package vroong.laas.bff.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.bff.dto.ApiResponse;
import vroong.laas.bff.model.OrderProjection;
import vroong.laas.bff.service.OrderQueryService;

/**
 * Order Query Controller
 * Order Projection 조회 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderQueryController {

    private final OrderQueryService queryService;

    /**
     * Order ID로 주문 조회
     * 
     * GET /api/v1/orders/{orderId}
     * 
     * Redis → MongoDB Fallback 패턴으로 조회
     * 
     * @param orderId Order ID
     * @return OrderProjection
     */
    @GetMapping("/{orderId}")
    public Mono<ApiResponse<OrderProjection>> getOrder(@PathVariable Long orderId) {
        log.debug("GET /api/v1/orders/{}", orderId);
        
        return queryService.getOrderProjection(orderId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Query failed: orderId={}, error={}", 
                        orderId, e.getMessage()));
    }
}

