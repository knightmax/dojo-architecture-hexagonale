package moon.mission.rescue.badapplication.controller;

import moon.mission.rescue.badapplication.model.Fleet;
import moon.mission.rescue.badapplication.request.RescueFleetRequest;
import moon.mission.rescue.badapplication.resource.FleetResource;
import moon.mission.rescue.badapplication.service.FleetAssemblerService;
import moon.mission.rescue.badapplication.service.FleetsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

/**
 * Service pour assembler une flotte de vaisseaux de sauvetage pour une mission de secours.
 */
@RestController
@RequestMapping("/rescue-mission")
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
    public ResponseEntity<FleetResource> getFleetById(@PathVariable String id) {
        Fleet fleet = fleets.getById(id);
        return ResponseEntity.ok(new FleetResource(fleet));
    }
}
