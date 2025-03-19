package moon.mission.rescue.adapters;

import moon.mission.rescue.domain.FleetsService;
import moon.mission.rescue.model.Fleet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class InMemoryFleetsService implements FleetsService {
    private final Map<UUID, Fleet> fleets = new HashMap<>();

    @Override
    public Fleet getById(UUID id) {
        return fleets.get(id);
    }

    @Override
    public Fleet save(Fleet fleet) {
        fleets.put(fleet.id(), fleet);
        return fleet;
    }
}
