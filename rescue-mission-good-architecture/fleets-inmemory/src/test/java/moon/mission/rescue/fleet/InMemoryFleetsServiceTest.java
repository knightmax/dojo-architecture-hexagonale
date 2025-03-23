package moon.mission.rescue.fleet;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class InMemoryFleetsServiceTest {

    private InMemoryFleetsService fleetsService;

    @BeforeEach
    void setUp() {
        fleetsService = new InMemoryFleetsService();
    }

    @Test
    void testSaveAndGetById() {
        UUID fleetId = UUID.randomUUID();
        Fleet fleet = new Fleet(fleetId, List.of(new StarShip("Starship 1", 100, 1000L)));

        fleetsService.save(fleet);
        Fleet retrievedFleet = fleetsService.getById(fleetId);

        assertEquals(fleet, retrievedFleet);
    }

    @Test
    void testGetByIdNotFound() {
        UUID fleetId = UUID.randomUUID();
        Fleet retrievedFleet = fleetsService.getById(fleetId);

        assertNull(retrievedFleet);
    }
}