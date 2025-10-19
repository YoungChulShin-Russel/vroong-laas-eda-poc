package vroong.laas.delivery.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    scanBasePackages = {
        "vroong.laas.delivery.api",
        "vroong.laas.delivery.core",
        "vroong.laas.delivery.infrastructure",
    }
)
public class ApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ApiApplication.class, args);
  }

}
