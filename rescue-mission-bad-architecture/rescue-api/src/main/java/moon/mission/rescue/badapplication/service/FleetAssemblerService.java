package moon.mission.rescue.badapplication.service;

import moon.mission.rescue.badapplication.client.SwapiResponse;
import moon.mission.rescue.badapplication.model.Fleet;
import moon.mission.rescue.badapplication.model.StarShip;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static moon.mission.rescue.badapplication.utils.StarShipsUtils.hasValidPassengersValue;
import static moon.mission.rescue.badapplication.utils.StarShipsUtils.toStarShip;

/**
 * Service pour assembler une flotte de vaisseaux spatiaux.
 */
@Service
public class FleetAssemblerService {

    private final WebClient webClient;
    private final FleetsService fleetsService;
    public static final long MINIMAL_CARGO_CAPACITY = 100000L;

    public FleetAssemblerService(
            final WebClient webClient,
            final FleetsService fleetsService) {
        this.webClient = webClient;
        this.fleetsService = fleetsService;
    }

    public Fleet forPassengers(int numberOfPassengers) {
        List<StarShip> starShips = getStarShipsHavingPassengersCapacity();
        List<StarShip> rescueStarShips = selectStarShips(numberOfPassengers, starShips);
        return fleetsService.save(new Fleet(rescueStarShips));
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
        return starShips().stream()
                .filter(starShip -> starShip.passengersCapacity() > 0)
                .sorted(comparingInt(StarShip::passengersCapacity))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<StarShip> starShips() {
        SwapiResponse swapiResponse = webClient.get()
                .uri("/starships")
                .retrieve()
                .bodyToMono(SwapiResponse.class)
                .block();

        if(swapiResponse == null || swapiResponse.results() == null) {
            return Collections.emptyList();
        }

        return swapiResponse.results().stream()
                .filter(hasValidPassengersValue())
                .map(toStarShip())
                .collect(toList());
    }
}
