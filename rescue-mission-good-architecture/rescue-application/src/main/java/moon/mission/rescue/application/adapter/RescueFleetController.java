package moon.mission.rescue.application.adapter;

import moon.mission.rescue.application.resource.FleetResource;
import moon.mission.rescue.application.request.RescueFleetRequest;
import moon.mission.rescue.domain.service.FleetAssemblerService;
import moon.mission.rescue.domain.service.FleetsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

/**
 * Service pour assembler une flotte de vaisseaux de sauvetage pour une mission de secours.
 */
@RestController
@RequestMapping("/rescue-fleets")
public class RescueFleetController {

    private final FleetAssemblerService fleetAssemblerService;
    private final FleetsService fleets;

    public RescueFleetController(
            final FleetAssemblerService fleetAssemblerService,
            final FleetsService fleets) {
        this.fleetAssemblerService = fleetAssemblerService;
        this.fleets = fleets;
    }

    /**
     * Assembler une flotte de vaisseaux de sauvetage pour une mission de secours.
     * @param rescueFleetRequest La demande de flotte à assembler
     * @return La flotte de vaisseaux de sauvetage assemblée
     */
    @PostMapping
    public ResponseEntity<FleetResource> assembleAFleet(@RequestBody RescueFleetRequest rescueFleetRequest) {
        var fleet = fleetAssemblerService.forPassengers(rescueFleetRequest.numberOfPassengers);
        return ResponseEntity.created(fromMethodCall(on(this.getClass()).getFleetById(fleet.id())).build().toUri())
                .body(new FleetResource(fleet));
    }

    /**
     * Obtenir une flotte de vaisseaux de sauvetage par son identifiant.
     * @param id L'identifiant de la flotte de vaisseaux de sauvetage
     * @return La flotte de vaisseaux de sauvetage
     */
    @GetMapping("/{id}")
    public ResponseEntity<FleetResource> getFleetById(@PathVariable UUID id) {
        return ResponseEntity.ok(new FleetResource(fleets.getById(id)));
    }
}
