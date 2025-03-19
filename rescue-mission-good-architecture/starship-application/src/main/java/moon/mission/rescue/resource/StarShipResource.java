package moon.mission.rescue.resource;

import moon.mission.rescue.model.StarShip;

public record StarShipResource(String name, int capacity, int passengersCapacity) {
    public StarShipResource(StarShip starShip) {
        this(starShip.name(),
                starShip.passengersCapacity(),
                starShip.passengersCapacity());
    }
}
