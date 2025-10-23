package vroong.laas.bff.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 설정
 * MSA 서비스 호출을 위한 HTTP 클라이언트
 */
@Configuration
public class WebClientConfig {

    @Value("${bff.command.msa-timeout-ms:10000}")
    private int timeoutMs;

    /**
     * 공통 WebClient.Builder
     * 타임아웃 및 커넥션 설정이 적용된 Builder
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
            .responseTimeout(Duration.ofMillis(timeoutMs))
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                    .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
            );

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
    
    /**
     * 범용 WebClient (MSA Client에서 사용)
     */
    @Bean
    @org.springframework.context.annotation.Primary
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean("orderServiceWebClient")
    public WebClient orderServiceWebClient(
            WebClient.Builder builder,
            @Value("${bff.services.order-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean("deliveryServiceWebClient")
    public WebClient deliveryServiceWebClient(
            WebClient.Builder builder,
            @Value("${bff.services.delivery-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }

    @Bean("dispatchServiceWebClient")
    public WebClient dispatchServiceWebClient(
            WebClient.Builder builder,
            @Value("${bff.services.dispatch-service.base-url}") String baseUrl) {
        return builder.baseUrl(baseUrl).build();
    }
}

