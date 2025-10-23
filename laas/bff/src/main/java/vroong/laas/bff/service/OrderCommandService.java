package vroong.laas.bff.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import vroong.laas.bff.client.OrderServiceClient;

import java.util.Map;

/**
 * Order Command Service
 * 주문 관련 명령 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCommandService {

    private final OrderServiceClient orderServiceClient;

    /**
     * 주문 생성
     * POST /api/v1/orders
     */
    public Mono<Map<String, Object>> createOrder(Map<String, Object> orderRequest) {
        log.info("Creating order");
        return orderServiceClient.createOrder(orderRequest);
    }
}

