package moon.mission.rescue.fleet.adapter.repository;

import moon.mission.rescue.fleet.model.FleetDocument;
import org.springframework.data.repository.CrudRepository;

public interface FleetsDocumentRepository extends CrudRepository<FleetDocument, String> {
}