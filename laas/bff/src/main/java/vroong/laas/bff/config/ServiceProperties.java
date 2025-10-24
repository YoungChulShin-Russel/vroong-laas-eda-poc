package vroong.laas.bff.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "gateway")
public class ServiceProperties {

  private ServiceConfig order;
  private ServiceConfig delivery;
  private ServiceConfig dispatch;
  private ServiceConfig readModel;

  @Getter
  @Setter
  public static class ServiceConfig {
    private String uri;
    private String routeId;
    private String path;
    private List<String> methods;  // HTTP 메서드 목록
  }
}

