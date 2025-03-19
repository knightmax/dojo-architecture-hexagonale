package moon.mission.rescue.config;

import moon.mission.rescue.domain.FleetAssemblerService;
import moon.mission.rescue.domain.FleetsService;
import moon.mission.rescue.domain.StarShipInventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfiguration {

    @Bean
    public FleetAssemblerService fleetAssemblerService(
            StarShipInventoryService starshipsInventory,
            FleetsService fleets) {
        return new FleetAssemblerService(starshipsInventory, fleets);
    }
}
