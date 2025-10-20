package vroong.laas.order.data.common.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "vroong.laas.order.data.entity")
@EnableJpaRepositories(basePackages = "vroong.laas.order.data.entity")
class JpaConfig {

}
