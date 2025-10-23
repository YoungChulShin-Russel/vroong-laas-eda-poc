package vroong.laas.bff.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import vroong.laas.bff.dto.ApiResponse;
import vroong.laas.bff.service.OrderCommandService;

import java.util.Map;

/**
 * Order Command Controller
 * 주문 관련 명령 API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    /**
     * 주문 생성
     * POST /api/v1/commands/orders
     */
    @PostMapping
    public Mono<ApiResponse<Map<String, Object>>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        log.info("POST /api/v1/commands/orders");
        return orderCommandService.createOrder(orderRequest)
                .map(ApiResponse::success)
                .doOnError(e -> log.error("Failed to create order: {}", e.getMessage()));
    }
}

