package moon.mission.rescue.domain;

import moon.mission.rescue.model.Fleet;

import java.util.UUID;

/**
 * Service pour gérer les flottes
 */
public interface FleetsService {
    Fleet getById(UUID id);

    Fleet save(Fleet fleet);
}
