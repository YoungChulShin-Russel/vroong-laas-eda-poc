package vroong.laas.readmodel.query.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import vroong.laas.readmodel.query.client.dto.OrderServiceResponse;

/**
 * Order Service Client (Reactive)
 * 
 * 역할: Write Service(Order Service)의 Query API 호출
 * 용도: Fallback (Read Model에 데이터가 없을 때)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final WebClient webClient;
    
    @Value("${msa.order.url:http://localhost:8081}")
    private String orderServiceUrl;
    
    /**
     * Order Service에서 Order 조회 (Fallback용, Reactive)
     * 
     * @param orderId Order ID
     * @return Mono<OrderServiceResponse>
     */
    public Mono<OrderServiceResponse> getOrder(Long orderId) {
        String url = orderServiceUrl + "/api/v1/orders/" + orderId;
        
        log.info("Fallback to Order Service: orderId={}, url={}", orderId, url);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(OrderServiceResponse.class)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(response -> log.info("Fallback success: orderId={}", orderId))
                .doOnError(e -> log.error("Fallback failed: orderId={}, error={}", orderId, e.getMessage()))
                .onErrorResume(e -> {
                    log.error("Fallback error, returning empty: orderId={}", orderId);
                    return Mono.empty();
                });
    }
}

