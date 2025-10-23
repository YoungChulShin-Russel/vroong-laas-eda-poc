package vroong.laas.bff.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 로깅 설정
 * Request ID 추적 및 요청/응답 로깅
 */
@Configuration
public class LoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_ATTR = "requestId";

    /**
     * 요청별 고유 ID 생성 및 로깅 필터
     */
    @Bean
    public WebFilter requestIdFilter() {
        return (ServerWebExchange exchange, WebFilterChain chain) -> {
            String requestId = exchange.getRequest()
                .getHeaders()
                .getFirst(REQUEST_ID_HEADER);
            
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }
            
            final String finalRequestId = requestId;
            exchange.getAttributes().put(REQUEST_ID_ATTR, finalRequestId);
            
            // 요청 로깅
            logger.info("[{}] {} {}", 
                finalRequestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath()
            );
            
            long startTime = System.currentTimeMillis();
            
            return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - startTime;
                    logger.info("[{}] Completed in {}ms - Status: {}", 
                        finalRequestId,
                        duration,
                        exchange.getResponse().getStatusCode()
                    );
                });
        };
    }
}

