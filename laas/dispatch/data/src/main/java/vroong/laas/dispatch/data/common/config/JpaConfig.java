package vroong.laas.dispatch.data.common.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "vroong.laas.dispatch.data.entity")
@EnableJpaRepositories(basePackages = "vroong.laas.dispatch.data.entity")
class JpaConfig {

}
