package vroong.laas.delivery.core.common.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "vroong.laas.delivery.core.domain")
@EnableJpaRepositories(basePackages = "vroong.laas.delivery.core.domain")
class JpaConfig {

}
