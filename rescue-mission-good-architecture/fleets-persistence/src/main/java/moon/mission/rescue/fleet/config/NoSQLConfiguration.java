package moon.mission.rescue.fleet.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "moon.mission.rescue.fleet.adapter.repository")
@EntityScan({ "moon.mission.rescue.fleet.model" })
public class NoSQLConfiguration {
}
