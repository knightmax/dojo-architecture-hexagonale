package moon.mission.rescue.badapplication.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SwapiStarShip(String name, String passengers, @JsonProperty("cargo_capacity") String cargoCapacity) {
}
