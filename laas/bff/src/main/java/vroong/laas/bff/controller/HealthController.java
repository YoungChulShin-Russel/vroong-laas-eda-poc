package vroong.laas.bff.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Health Check 컨트롤러
 * 서버 상태 확인용 엔드포인트
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public Mono<Map<String, Object>> health() {
        return Mono.just(Map.of(
            "status", "UP",
            "service", "bff",
            "timestamp", LocalDateTime.now()
        ));
    }

    @GetMapping("/ready")
    public Mono<Map<String, String>> ready() {
        return Mono.just(Map.of(
            "status", "READY"
        ));
    }

    @GetMapping("/live")
    public Mono<Map<String, String>> live() {
        return Mono.just(Map.of(
            "status", "ALIVE"
        ));
    }
}

