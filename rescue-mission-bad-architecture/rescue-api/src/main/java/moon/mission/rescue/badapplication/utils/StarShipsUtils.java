package moon.mission.rescue.badapplication.utils;

import moon.mission.rescue.badapplication.client.SwapiStarShip;
import moon.mission.rescue.badapplication.model.StarShip;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;

public class StarShipsUtils {
    private final static List<String> invalidCapacitiesValues = asList("n/a", "unknown");

    private StarShipsUtils() {
        // Utility class
    }

    public static Function<SwapiStarShip, StarShip> toStarShip() {
        return swapiStarShip ->
                new StarShip(
                        swapiStarShip.name(),
                        parseInt(swapiStarShip.passengers().replaceAll(",", "")),
                        Long.parseLong(swapiStarShip.cargoCapacity()));
    }

    public static Predicate<SwapiStarShip> hasValidPassengersValue() {
        return swapiStarShip -> swapiStarShip.passengers() != null
                && !invalidCapacitiesValues.contains(swapiStarShip.passengers())
                && !invalidCapacitiesValues.contains(swapiStarShip.cargoCapacity());
    }
}
