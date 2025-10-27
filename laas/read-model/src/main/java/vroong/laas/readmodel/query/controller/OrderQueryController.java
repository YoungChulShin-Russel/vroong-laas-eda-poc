package vroong.laas.readmodel.query.controller;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.readmodel.query.controller.response.ApiResponse;
import vroong.laas.readmodel.query.controller.response.OrderResponse;
import vroong.laas.readmodel.query.controller.response.OrderTimelineResponse;
import vroong.laas.readmodel.query.service.OrderQueryService;

import java.util.List;
import java.time.LocalDateTime;

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
    name = "readmodel.features.api.enabled",
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
    public Mono<ApiResponse<OrderResponse>> getOrder(@PathVariable Long orderId) {
        log.info("GET /api/v1/orders/{}", orderId);
        
        return queryService.getOrder(orderId)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to get order projection: orderId={}, error={}", 
                        orderId, e.getMessage()));
    }

    /**
     * Order Timeline 조회
     * 
     * GET /api/v1/orders/{orderId}/timeline
     * 
     * @param orderId Order ID
     * @return List<OrderTimelineResponse>
     */
    @GetMapping("/{orderId}/timeline")
    public Mono<ApiResponse<List<OrderTimelineResponse>>> getOrderTimeline(@PathVariable Long orderId) {
        log.info("GET /api/v1/orders/{}/timeline", orderId);
        
        return queryService.getOrderTimeline(orderId)
                .map(timeline -> timeline.stream()
                        .map(OrderTimelineResponse::fromModel)
                        .toList())
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to get order timeline: orderId={}, error={}", 
                        orderId, e.getMessage()));
    }

    /**
     * 기사별 주문 조회
     * 
     * GET /api/v1/orders/agent/{agentId}?startDate=2024-01-01&endDate=2024-01-31&deliveryStatus=DELIVERED
     * 
     * @param agentId 기사 ID
     * @param startDate 조회 시작일 (필수)
     * @param endDate 조회 종료일 (필수)
     * @param deliveryStatus 배송 상태 (옵션)
     * @return List<OrderResponse>
     */
    @GetMapping("/agent/{agentId}")
    public Mono<ApiResponse<List<OrderResponse>>> getOrdersByAgent(
            @PathVariable Long agentId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(required = false) String deliveryStatus) {
        
        log.info("GET /api/v1/orders/agent/{} - startDate={}, endDate={}, deliveryStatus={}", 
                agentId, startDate, endDate, deliveryStatus);
        
        return queryService.getOrdersByAgent(agentId, startDate, endDate, deliveryStatus)
                .map(orders -> orders.stream()
                        .map(OrderResponse::fromOrderAggregate)
                        .toList())
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to get orders by agent: agentId={}, error={}", 
                        agentId, e.getMessage()));
    }

    /**
     * 관리자용 주문 조회
     * 
     * GET /api/v1/orders/admin?startDate=2024-01-01&endDate=2024-01-31&agentId=123&orderStatus=ACTIVE
     * 
     * @param startDate 조회 시작일 (필수)
     * @param endDate 조회 종료일 (필수)
     * @param agentId 기사 ID (옵션)
     * @param orderStatus 주문 상태 (옵션)
     * @return List<OrderResponse>
     */
    @GetMapping("/admin")
    public Mono<ApiResponse<List<OrderResponse>>> getOrdersForAdmin(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) String orderStatus) {
        
        Instant startInstant = Instant.parse(startDate);
        Instant endInstant = Instant.parse(endDate);
        
        log.info("GET /api/v1/orders/admin - startDate={}, endDate={}, agentId={}, orderStatus={}", 
                startInstant, endInstant, agentId, orderStatus);
        
        return queryService.getOrdersForAdmin(startInstant, endInstant, agentId, orderStatus)
                .map(orders -> orders.stream()
                        .map(OrderResponse::fromOrderAggregate)
                        .toList())
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to get orders for admin: error={}", e.getMessage()));
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public Mono<String> health() {
        return Mono.just("OK");
    }
}

