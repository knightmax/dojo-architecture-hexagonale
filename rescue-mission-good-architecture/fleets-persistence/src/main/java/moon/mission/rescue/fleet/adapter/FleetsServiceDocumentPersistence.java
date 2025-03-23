package moon.mission.rescue.fleet.adapter;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.service.FleetsService;
import moon.mission.rescue.fleet.adapter.repository.FleetsDocumentRepository;
import moon.mission.rescue.fleet.model.FleetDocument;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FleetsServiceDocumentPersistence implements FleetsService {

    private final FleetsDocumentRepository repository;
    public FleetsServiceDocumentPersistence(FleetsDocumentRepository repository) {
        this.repository = repository;
    }

    @Override
    public Fleet getById(String id) {
        Optional<FleetDocument> byId = repository.findById(id);

        return byId
                .map(FleetDocument::toDomain)
                .orElse(null);
    }

    @Override
    public Fleet save(Fleet fleet) {
        FleetDocument saved = repository.save(new FleetDocument(fleet.starships()));
        return new Fleet(saved.getId(), saved.getStarships());
    }
}
