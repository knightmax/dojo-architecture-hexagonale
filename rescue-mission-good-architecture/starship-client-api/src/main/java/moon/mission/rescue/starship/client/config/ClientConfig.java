package moon.mission.rescue.starship.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ClientConfig {

    private final static Logger LOGGER = LoggerFactory.getLogger(ClientConfig.class);

    @Bean
    public WebClient swapiClient(@Value("${starship.inventory.url}") final String url) {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Accept", "application/json")
                .filter((request, next) -> {
                    LOGGER.info("Request: {} {}", request.method(), request.url());
                    return next.exchange(request);
                })
                .build();
    }
}
