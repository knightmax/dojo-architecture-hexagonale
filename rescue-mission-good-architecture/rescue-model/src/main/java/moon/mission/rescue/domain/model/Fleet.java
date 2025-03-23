package moon.mission.rescue.domain.model;

import java.util.List;
import java.util.UUID;

public record Fleet (UUID id, List<StarShip> starships){
    public Fleet(List<StarShip> starships) {
        this(UUID.randomUUID(),starships);
    }
}

