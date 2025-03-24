package moon.mission.rescue.badapplication.repository;

import moon.mission.rescue.badapplication.entity.FleetDocument;
import org.springframework.data.repository.CrudRepository;

public interface FleetsDocumentRepository extends CrudRepository<FleetDocument, String> {
}