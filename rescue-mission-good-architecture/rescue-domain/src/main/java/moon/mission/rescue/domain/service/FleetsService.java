package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.Fleet;

/**
 * Service pour gérer les flottes
 */
public interface FleetsService {
    Fleet getById(String id);

    Fleet save(Fleet fleet);
}
