package moon.mission.rescue.domain;

import moon.mission.rescue.model.Fleet;
import moon.mission.rescue.model.StarShip;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

/**
 * Service pour assembler une flotte de vaisseaux spatiaux.
 */
public class FleetAssemblerService {

    private final StarShipInventoryService starshipsInventory;
    private final FleetsService fleets;
    static final long MINIMAL_CARGO_CAPACITY = 100000L;

    public FleetAssemblerService(StarShipInventoryService starShipsInventory, FleetsService fleets) {
        this.starshipsInventory = starShipsInventory;
        this.fleets = fleets;
    }

    public Fleet forPassengers(int numberOfPassengers) {
        List<StarShip> starShips = getStarShipsHavingPassengersCapacity();
        List<StarShip> rescueStarShips = selectStarShips(numberOfPassengers, starShips);
        return fleets.save(new Fleet(rescueStarShips));
    }

    private List<StarShip> selectStarShips(int numberOfPassengers, List<StarShip> starShips) {
        starShips.removeIf(doesntHaveEnoughCargoCapacity());
        List<StarShip> rescueStarShips = new ArrayList<>();
        while (numberOfPassengers > 0) {
            var starShip = starShips.removeFirst();
            numberOfPassengers -= starShip.passengersCapacity();
            rescueStarShips.add(starShip);
        }
        return rescueStarShips;
    }

    private Predicate<? super StarShip> doesntHaveEnoughCargoCapacity() {
        return starShip -> starShip.cargoCapacity() < MINIMAL_CARGO_CAPACITY;
    }

    private List<StarShip> getStarShipsHavingPassengersCapacity() {
        return starshipsInventory.starShips().stream()
                .filter(starShip -> starShip.passengersCapacity() > 0)
                .sorted(comparingInt(StarShip::passengersCapacity))
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
