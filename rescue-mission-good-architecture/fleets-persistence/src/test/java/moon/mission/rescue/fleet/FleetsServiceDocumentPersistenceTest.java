package moon.mission.rescue.fleet;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.fleet.adapter.FleetsServiceDocumentPersistence;
import moon.mission.rescue.fleet.adapter.repository.FleetsDocumentRepository;
import moon.mission.rescue.fleet.model.FleetDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class FleetsServiceDocumentPersistenceTest {

    @Mock
    private FleetsDocumentRepository repository;

    @InjectMocks
    private FleetsServiceDocumentPersistence service;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetById_FleetExists() {
        List<StarShip> starShips = List.of(new StarShip("starShips", 100, 1000L));
        FleetDocument fleetDocument = new FleetDocument(starShips);
        fleetDocument.setId("1");
        when(repository.findById("1")).thenReturn(Optional.of(fleetDocument));

        Fleet result = service.getById("1");

        assertEquals("1", result.id());
        assertEquals(starShips, result.starships());
    }

    @Test
    public void testGetById_FleetDoesNotExist() {
        when(repository.findById("1")).thenReturn(Optional.empty());

        Fleet result = service.getById("1");

        assertNull(result);
    }

    @Test
    public void testSave() {
        List<StarShip> starShips = List.of(new StarShip("starShips", 100, 1000L));
        Fleet fleet = new Fleet("1", starShips);
        FleetDocument fleetDocument = new FleetDocument(starShips);
        fleetDocument.setId("1");
        when(repository.save(any(FleetDocument.class))).thenReturn(fleetDocument);

        Fleet result = service.save(fleet);

        assertEquals("1", result.id());
        assertEquals(starShips, result.starships());
    }
}
