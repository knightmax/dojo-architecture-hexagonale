package moon.mission.rescue.application.resource;

import moon.mission.rescue.domain.model.Fleet;

import java.util.List;

import static java.util.stream.Collectors.toList;

public record FleetResource(String id, List<StarShipResource> starships) {
    public FleetResource(Fleet fleet) {
        this(fleet.id(), fleet.starships().stream().map(StarShipResource::new).collect(toList()));
    }
}
