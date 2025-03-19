package moon.mission.rescue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.contract.wiremock.WireMockConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import rebelsrescue.swapi.SwapiUrlTransformer;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = "swapi.base-uri=http://localhost:${wiremock.server.port}")
@AutoConfigureWireMock(port = 0)
class ApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private WireMockServer mockSwapi;

    private BasicJsonTester json = new BasicJsonTester(this.getClass());

    @Test
    void should_assemble_a_fleet() throws IOException {
        configureSwapiMock();
        var request = createRequest();

        var response = restTemplate.postForEntity("http://localhost:%d/rescueFleets".formatted(port), request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(json.from(response.getBody())).hasJsonPath("@.id");
        assertThat(json.from(response.getBody())).extractingJsonPathArrayValue("@.starships").hasSize(1);
        assertThat(json.from(response.getBody())).extractingJsonPathStringValue("@.starships[0].name").isEqualTo("CR90 corvette");
        assertThat(json.from(response.getBody())).extractingJsonPathNumberValue("@.starships[0].capacity").isEqualTo(600);
    }

    private void configureSwapiMock() {
        mockSwapi.stubFor(
                get(urlPathMatching("/api/starships"))
                        .willReturn(
                                aResponse()
                                        .withBodyFile("payloads/swapi-page1.json")
                                        .withStatus(200)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)));

        mockSwapi.stubFor(
                get(urlPathMatching("/api/starships/")).withQueryParam("page", new EqualToPattern("2"))
                        .willReturn(
                                aResponse()
                                        .withBodyFile("payloads/swapi-page2.json")
                                        .withStatus(200)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)));
    }

    private HttpEntity<String> createRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        return new HttpEntity<>("{\"numberOfPassengers\" : 3}", headers);
    }

    @TestConfiguration
    static class WireMockConfiguration {

        @Bean
        WireMockConfigurationCustomizer optionsCustomizer() {
            return config -> config.extensions(SwapiUrlTransformer.class);
        }
    }


}
