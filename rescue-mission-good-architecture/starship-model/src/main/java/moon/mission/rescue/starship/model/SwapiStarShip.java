package moon.mission.rescue.starship.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SwapiStarShip(String name, String passengers, @JsonProperty("cargo_capacity") String cargoCapacity) {
}
