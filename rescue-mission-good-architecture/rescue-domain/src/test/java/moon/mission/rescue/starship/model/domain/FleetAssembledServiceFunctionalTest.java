package moon.mission.rescue.starship.model.domain;

import moon.mission.rescue.domain.service.FleetAssemblerService;
import moon.mission.rescue.domain.service.FleetsService;
import moon.mission.rescue.domain.service.StarShipInventoryService;
import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static moon.mission.rescue.domain.service.FleetAssemblerService.MINIMAL_CARGO_CAPACITY;
import static org.assertj.core.api.Assertions.assertThat;

class FleetAssembledServiceFunctionalTest {

    @Test
    void should_assemble_a_fleet_for_1050_passengers() {
        //Given
        var starShips = asList(
                new StarShip("no-passenger-ship", 0, 0L),
                new StarShip("xs", 10, 1000L),
                new StarShip("s", 50, 50000L),
                new StarShip("m", 200, 70000L),
                new StarShip("l", 800, 150000L),
                new StarShip("xl", 2000, 500000L));
        var numberOfPassengers = 1050;

        StarShipInventoryService starShipsInventory = Mockito.mock(StarShipInventoryService.class);
        FleetsService fleets = Mockito.mock(FleetsService.class);

        Mockito.when(starShipsInventory.starShips()).thenReturn(starShips);
        Mockito.when(fleets.save(Mockito.any(Fleet.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(fleets.getById(Mockito.any())).thenReturn(new Fleet(asList(starShips.get(4), starShips.get(5))));

        FleetAssemblerService assembleAFleet = new FleetAssemblerService(starShipsInventory, fleets);

        //When
        Fleet fleet = assembleAFleet.forPassengers(numberOfPassengers);

        //Then
        System.out.println(fleet);
        assertThat(fleet.starships())
                .has(enoughCapacityForThePassengers(numberOfPassengers))
                .allMatch(hasPassengersCapacity())
                .allMatch(hasEnoughCargoCapacity(), "hasEnoughCargoCapacity");

        Fleet actual = fleets.getById(fleet.id());
        assertThat(actual.starships()).isEqualTo(fleet.starships());
    }

    private Predicate<? super StarShip> hasPassengersCapacity() {
        return starShip -> starShip.passengersCapacity() > 0;
    }

    private Predicate<? super StarShip> hasEnoughCargoCapacity() {
        return starShip -> starShip.cargoCapacity() >= MINIMAL_CARGO_CAPACITY;
    }

    private Condition<? super List<? extends StarShip>> enoughCapacityForThePassengers(int numberOfPassengers) {
        return new Condition<>(
                starShips ->
                        starShips.stream()
                                .map(StarShip::passengersCapacity)
                                .reduce(0, Integer::sum) >= numberOfPassengers,
                "passengersCapacity check");
    }

}
