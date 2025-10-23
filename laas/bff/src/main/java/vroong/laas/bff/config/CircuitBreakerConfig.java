package vroong.laas.bff.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Circuit Breaker 설정
 * MSA 호출 시 장애 격리를 위한 Circuit Breaker 패턴 적용
 */
@Configuration
public class CircuitBreakerConfig {

    @Value("${bff.circuit-breaker.failure-rate-threshold:50}")
    private float failureRateThreshold;

    @Value("${bff.circuit-breaker.slow-call-rate-threshold:50}")
    private float slowCallRateThreshold;

    @Value("${bff.circuit-breaker.slow-call-duration-threshold:3s}")
    private Duration slowCallDurationThreshold;

    @Value("${bff.circuit-breaker.permitted-calls-in-half-open:3}")
    private int permittedCallsInHalfOpen;

    @Value("${bff.circuit-breaker.sliding-window-size:10}")
    private int slidingWindowSize;

    @Value("${bff.circuit-breaker.wait-duration-in-open-state:60s}")
    private Duration waitDurationInOpenState;

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        io.github.resilience4j.circuitbreaker.CircuitBreakerConfig config = 
            io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.custom()
                .failureRateThreshold(failureRateThreshold)
                .slowCallRateThreshold(slowCallRateThreshold)
                .slowCallDurationThreshold(slowCallDurationThreshold)
                .permittedNumberOfCallsInHalfOpenState(permittedCallsInHalfOpen)
                .slidingWindowType(SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(slidingWindowSize)
                .waitDurationInOpenState(waitDurationInOpenState)
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry() {
        TimeLimiterConfig config = TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(5))
                .cancelRunningFuture(true)
                .build();

        return TimeLimiterRegistry.of(config);
    }
}

