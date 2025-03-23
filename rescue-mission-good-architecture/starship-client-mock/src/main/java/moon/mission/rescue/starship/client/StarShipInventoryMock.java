package moon.mission.rescue.starship.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import moon.mission.rescue.domain.service.StarShipInventoryService;
import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.starship.model.SwapiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static moon.mission.rescue.starship.utils.StarShipsUtils.hasValidPassengersValue;
import static moon.mission.rescue.starship.utils.StarShipsUtils.toStarShip;

@Service
public class StarShipInventoryMock implements StarShipInventoryService {

    private final ObjectMapper objectMapper;
    private final Resource resourceFile;

    public StarShipInventoryMock(
            final ObjectMapper objectMapper,
            @Value("classpath:payloads/swapi-starships.json") Resource resourceFile
    ) {
        this.objectMapper = objectMapper;
        this.resourceFile = resourceFile;
    }

    @Override
    public List<StarShip> starShips() {

        List<StarShip> starShips;
        try {
            SwapiResponse swapiResponse = objectMapper.readValue(resourceFile.getInputStream(), SwapiResponse.class);
            starShips = swapiResponse.results().stream()
                    .filter(hasValidPassengersValue())
                    .map(toStarShip())
                    .collect(toList());
        } catch (IOException e) {
            starShips = fallbackShips();
        }

        return starShips;
    }

    private static List<StarShip> fallbackShips() {
        return List.of(
                new StarShip("1", 600, 3000000L),
                new StarShip("2", 600, 3000000L),
                new StarShip("3", 600, 3000000L),
                new StarShip("4", 600, 3000000L),
                new StarShip("5", 600, 3000000L),
                new StarShip("6", 600, 3000000L),
                new StarShip("7", 600, 3000000L),
                new StarShip("8", 600, 3000000L),
                new StarShip("9", 600, 3000000L),
                new StarShip("10", 600, 3000000L)
        );
    }
}
