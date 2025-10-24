package vroong.laas.bff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class ReadModelGatewayConfig {

  private final ServiceProperties serviceProperties;

  @Bean
  public RouteLocator readModelRoutes(RouteLocatorBuilder builder) {
    var config = serviceProperties.getReadModel();
    var methods = config.getMethods().stream()
        .map(HttpMethod::valueOf)
        .toArray(HttpMethod[]::new);
    
    return builder.routes()
        // Query 라우팅 - Read-model 서비스 (모든 GET 요청)
        .route(config.getRouteId(), r -> r
            .path(config.getPath())
            .and()
            .method(methods)
            .uri(config.getUri()))
        
        .build();
  }
}

