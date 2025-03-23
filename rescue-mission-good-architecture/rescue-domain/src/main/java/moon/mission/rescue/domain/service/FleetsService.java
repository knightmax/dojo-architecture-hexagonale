package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.Fleet;

import java.util.UUID;

/**
 * Service pour gérer les flottes
 */
public interface FleetsService {
    Fleet getById(UUID id);

    Fleet save(Fleet fleet);
}
