package rebelsrescue.swapi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import rebelsrescue.domain.model.StarShip;
import rebelsrescue.domain.service.StarShipInventoryService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

@Component
public class SwapiClient implements StarShipInventoryService {
    private final RestTemplate restTemplate;
    private final List<String> invalidCapacitiesValues = asList("n/a", "unknown");

    @Value("${swapi.base-uri}")
    private String swapiBaseUri;

    public SwapiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    public List<StarShip> starShips() {
        List<StarShip> starShips = new ArrayList<>();

        var nextPageUrl = swapiBaseUri + "/api/starships";
        while (nextPageUrl != null) {
            var swapiResponse = getStarShipsFromSwapi(nextPageUrl);
            starShips.addAll(convertSwapiResponseToStarShips(swapiResponse));
            nextPageUrl = swapiResponse.next();
        }
        return starShips;
    }

    private List<StarShip> convertSwapiResponseToStarShips(SwapiResponse swapiResponse) {
        return swapiResponse.results().stream()
                .filter(hasValidPassengersValue())
                .map(toStarShip())
                .collect(toList());
    }

    private Function<SwapiStarShip, StarShip> toStarShip() {
        return swapiStarShip ->
                new StarShip(
                        swapiStarShip.name(),
                        parseInt(swapiStarShip.passengers().replaceAll(",", "")),
                        new BigDecimal(swapiStarShip.cargoCapacity()));
    }

    private Predicate<SwapiStarShip> hasValidPassengersValue() {
        return swapiStarShip -> swapiStarShip.passengers() != null
                && !invalidCapacitiesValues.contains(swapiStarShip.passengers())
                && !invalidCapacitiesValues.contains(swapiStarShip.cargoCapacity());
    }

    private SwapiResponse getStarShipsFromSwapi(String url) {
        return restTemplate.getForObject(url, SwapiResponse.class);
    }
}
