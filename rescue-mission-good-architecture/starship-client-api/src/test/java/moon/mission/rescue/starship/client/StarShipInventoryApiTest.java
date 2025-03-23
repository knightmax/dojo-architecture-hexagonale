package moon.mission.rescue.starship.client;

import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.starship.model.SwapiResponse;
import moon.mission.rescue.starship.model.SwapiStarShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class StarShipInventoryApiTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    public void testStarShips_Success() {
        StarShipInventoryApi starShipInventoryApi = new StarShipInventoryApi(webClient);

        SwapiResponse swapiResponse = new SwapiResponse(List.of(new SwapiStarShip("1", "600", "3000000")));
        when(responseSpec.bodyToMono(SwapiResponse.class)).thenReturn(Mono.just(swapiResponse));

        List<StarShip> starShips = starShipInventoryApi.starShips();

        assertEquals(1, starShips.size());
    }

    @Test
    public void testStarShips_Failure() {
        StarShipInventoryApi starShipInventoryApi = new StarShipInventoryApi(webClient);

        SwapiResponse swapiResponse = new SwapiResponse(Collections.emptyList());
        when(responseSpec.bodyToMono(SwapiResponse.class)).thenReturn(Mono.just(swapiResponse));

        List<StarShip> starShips = starShipInventoryApi.starShips();

        assertTrue(starShips.isEmpty());
    }
}
