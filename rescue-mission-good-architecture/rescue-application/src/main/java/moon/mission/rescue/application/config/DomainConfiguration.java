package moon.mission.rescue.application.config;

import moon.mission.rescue.domain.service.FleetAssemblerService;
import moon.mission.rescue.domain.service.FleetsService;
import moon.mission.rescue.domain.service.StarShipInventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public FleetAssemblerService fleetAssemblerService(
            final StarShipInventoryService starshipsInventory,
            final FleetsService fleets) {
        return new FleetAssemblerService(starshipsInventory, fleets);
    }
}
