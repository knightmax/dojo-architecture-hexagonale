package moon.mission.rescue;

import moon.mission.rescue.badapplication.model.Fleet;
import moon.mission.rescue.badapplication.model.StarShip;
import moon.mission.rescue.badapplication.service.FleetAssemblerService;
import moon.mission.rescue.badapplication.service.FleetsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BadApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class BadApplicationTest {

    @MockitoBean
    private FleetAssemblerService fleetAssemblerService;

    @MockitoBean
    private FleetsService fleetsService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAssembleAFleet() throws Exception {

        Fleet fleet = new Fleet("fleet123", List.of(new StarShip("starship123", 100, 10000L)));
        when(fleetAssemblerService.forPassengers(anyInt())).thenReturn(fleet);

        mockMvc.perform(post("/rescue-mission")
                .contentType("application/json")
                .content("{\"numberOfPassengers\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("fleet123"));
    }

    @Test
    public void testGetFleetById() throws Exception {

        Fleet fleet = new Fleet("fleet123", List.of(new StarShip("starship123", 100, 10000L)));
        when(fleetsService.getById(anyString())).thenReturn(fleet);

        mockMvc.perform(get("/rescue-mission/fleet123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("fleet123"));
    }
}
