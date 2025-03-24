package moon.mission.rescue.badapplication.service;

import moon.mission.rescue.badapplication.entity.FleetDocument;
import moon.mission.rescue.badapplication.model.Fleet;
import moon.mission.rescue.badapplication.repository.FleetsDocumentRepository;
import org.springframework.stereotype.Service;

/**
 * Service pour gérer les flottes
 */
@Service
public class FleetsService {

    private final FleetsDocumentRepository fleetsRepository;
    public FleetsService(FleetsDocumentRepository fleetsRepository) {
        this.fleetsRepository = fleetsRepository;
    }

    public Fleet getById(String id) {
        return fleetsRepository.findById(id)
                .map(document -> new Fleet(document.getId(), document.getStarships()))
                .orElse(null);
    }

    public Fleet save(Fleet fleet) {
        FleetDocument fleetDocument = new FleetDocument(fleet.starships());
        FleetDocument saved = fleetsRepository.save(fleetDocument);
        return new Fleet(saved.getId(), saved.getStarships());
    }
}
