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

