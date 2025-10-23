package vroong.laas.bff.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * MongoDB Reactive 설정
 * BFF 서버에서 Projection 데이터 조회용
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = "vroong.laas.bff.repository.mongo")
public class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authenticationDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        // MongoDB Connection String 생성
        String connectionString = String.format(
            "mongodb://%s:%s@%s:%d/%s?authSource=%s",
            username, password, host, port, database, authenticationDatabase
        );

        // Reactive MongoDB 클라이언트 설정
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(connectionString))
            .build();

        return MongoClients.create(settings);
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        ReactiveMongoTemplate template = new ReactiveMongoTemplate(
            reactiveMongoClient(), 
            getDatabaseName()
        );
        
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        // "_class" 필드 제거 (타입 정보 저장 안 함)
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        return template;
    }
}

