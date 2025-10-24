package vroong.laas.bff.config;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

@Configuration
@RequiredArgsConstructor
public class DeliveryGatewayConfig {

  private final ServiceProperties serviceProperties;

  @Bean
  public RouteLocator deliveryRoutes(RouteLocatorBuilder builder) {
    var config = serviceProperties.getDelivery();
    var methods = config.getMethods().stream()
        .map(HttpMethod::valueOf)
        .toArray(HttpMethod[]::new);
    
    return builder.routes()
        // Command 라우팅 - Delivery 서비스
        .route(config.getRouteId(), r -> r
            .path(config.getPath())
            .and()
            .method(methods)
            .uri(config.getUri()))
        
        .build();
  }
}

