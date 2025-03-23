package moon.mission.rescue.starship.client;

import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.domain.service.StarShipInventoryService;
import moon.mission.rescue.starship.model.SwapiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static moon.mission.rescue.starship.utils.StarShipsUtils.hasValidPassengersValue;
import static moon.mission.rescue.starship.utils.StarShipsUtils.toStarShip;

@Service
public class StarShipInventoryApi implements StarShipInventoryService {

    private final WebClient webClient;
    public StarShipInventoryApi(final WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
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
