package moon.mission.rescue.starship.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.starship.model.SwapiResponse;
import moon.mission.rescue.starship.model.SwapiStarShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StarShipInventoryMockTest {

    private ObjectMapper objectMapper;
    private StarShipInventoryMock starShipInventoryMock;

    @BeforeEach
    void setUp() {
        objectMapper = mock(ObjectMapper.class);
        Resource resourceFile = new ClassPathResource("payloads/swapi-starships.json");
        starShipInventoryMock = new StarShipInventoryMock(objectMapper, resourceFile);
    }

    @Test
    void testStarShips() throws IOException {
        SwapiResponse swapiResponse = mock(SwapiResponse.class);
        when(objectMapper.readValue(any(InputStream.class), eq(SwapiResponse.class))).thenReturn(swapiResponse);
        when(swapiResponse.results()).thenReturn(List.of(new SwapiStarShip("1", "600", "3000000")));

        List<StarShip> starShips = starShipInventoryMock.starShips();

        assertNotNull(starShips);
        assertFalse(starShips.isEmpty());
        assertEquals(1, starShips.size());
    }

    @Test
    void testStarShipsFallback() throws IOException {
        when(objectMapper.readValue(any(InputStream.class), eq(SwapiResponse.class))).thenThrow(new IOException());

        List<StarShip> starShips = starShipInventoryMock.starShips();

        assertNotNull(starShips);
        assertFalse(starShips.isEmpty());
        assertEquals(10, starShips.size());
    }
}