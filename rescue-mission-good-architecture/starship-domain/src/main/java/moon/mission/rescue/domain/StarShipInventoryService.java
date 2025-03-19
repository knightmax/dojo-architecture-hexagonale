package moon.mission.rescue.domain;

import moon.mission.rescue.model.StarShip;

import java.util.List;

/**
 * Service pour gérer les vaisseaux
 */
public interface StarShipInventoryService {
    List<StarShip> starShips();
}
