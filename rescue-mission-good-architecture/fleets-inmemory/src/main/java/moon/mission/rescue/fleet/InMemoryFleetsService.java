package moon.mission.rescue.fleet;

import moon.mission.rescue.domain.service.FleetsService;
import moon.mission.rescue.domain.model.Fleet;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class InMemoryFleetsService implements FleetsService {
    private final Map<String, Fleet> fleets = new HashMap<>();

    @Override
    public Fleet getById(String id) {
        return fleets.get(id);
    }

    @Override
    public Fleet save(Fleet fleet) {
        fleets.put(fleet.id(), fleet);
        return fleet;
    }
}
