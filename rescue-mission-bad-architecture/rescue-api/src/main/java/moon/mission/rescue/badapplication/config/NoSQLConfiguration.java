package moon.mission.rescue.badapplication.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "moon.mission.rescue.badapplication.repository")
@EntityScan({ "moon.mission.rescue.badapplication.entity" })
public class NoSQLConfiguration {
}
