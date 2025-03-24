package moon.mission.rescue.badapplication.resource;

import moon.mission.rescue.badapplication.model.StarShip;

public record StarShipResource(String name, int capacity, int passengersCapacity) {
    public StarShipResource(StarShip starShip) {
        this(starShip.name(),
                starShip.passengersCapacity(),
                starShip.passengersCapacity());
    }
}
