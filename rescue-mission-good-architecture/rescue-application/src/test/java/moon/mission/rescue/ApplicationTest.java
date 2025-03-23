package moon.mission.rescue;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.domain.service.FleetsService;
import moon.mission.rescue.domain.service.StarShipInventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
class ApplicationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StarShipInventoryService starShipInventoryService;

    @MockitoBean
    private FleetsService fleetsService;

    @BeforeEach
    void setUp() {
        Mockito.when(starShipInventoryService.starShips())
                .thenReturn(List.of(
                        new StarShip("StarShip1", 1000, 100000L),
                        new StarShip("StarShip2", 1000, 100000L),
                        new StarShip("StarShip3", 1000, 100000L)));

        Mockito.when(fleetsService.save(Mockito.any(Fleet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mockito.when(fleetsService.getById(Mockito.any(UUID.class)))
                .thenReturn(new Fleet(UUID.randomUUID(), List.of(new StarShip("StarShip1", 1000, 100000L))));
    }

    @Test
    void should_assemble_a_fleet() throws Exception {

        var request = """
                {
                    "numberOfPassengers": 1000
                }
                """;

        mockMvc.perform(post("/rescue-fleets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().is(CREATED.value()))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.starships[0].passengersCapacity").value(1000));
    }
}