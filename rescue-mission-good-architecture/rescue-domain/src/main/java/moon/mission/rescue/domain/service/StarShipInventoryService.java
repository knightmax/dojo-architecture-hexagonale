package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.StarShip;

import java.util.List;

/**
 * Service pour gérer les vaisseaux
 */
public interface StarShipInventoryService {
    List<StarShip> starShips();
}
