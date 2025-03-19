package moon.mission.rescue.resource;

import moon.mission.rescue.model.Fleet;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

public record FleetResource(UUID id, List<StarShipResource> starships) {
    public FleetResource(Fleet fleet) {
        this(fleet.id(), fleet.starships().stream().map(StarShipResource::new).collect(toList()));
    }
}
