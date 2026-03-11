# Hexagonal Architecture DOJO Project

This workspace is a **DOJO dedicated to mastering Hexagonal Architecture** (Ports & Adapters). This file provides project-specific context and conventions. For comprehensive hexagonal architecture rules and enforcement strategies, see [`.github/instructions/hexagonal-architecture.instructions.md`](.github/instructions/hexagonal-architecture.instructions.md) — this is a reusable instruction file that applies to all Java code in this and other projects.

## Project-Specific Package Structure

This DOJO project uses the following Maven module organization:

### Domain Module: `rescue-domain/`
Package: `moon.mission.rescue.domain.**`
- **Services**: `moon.mission.rescue.domain.service.*` — Port interfaces (`StarShipInventoryService`, `FleetsService`) and domain services (`FleetAssemblerService`)
- **Models**: `moon.mission.rescue.domain.model.*` — Domain entities and value objects

### Model Module: `rescue-model/`
Package: `moon.mission.rescue.domain.model.*`
- Immutable domain models (records): `StarShip`, `Fleet`

### Application Module: `rescue-application/`
Package: `moon.mission.rescue.application.**`
- **Config**: `application.config.*` — Spring `@Configuration` classes, dependency wiring
- **Adapters**: `application.adapter.*` — REST controller (`RescueFleetController`)
- **Request/Response**: `application.request.*`, `application.resource.*` — DTOs and API resources

### Adapter Modules (Independent, Swappable)

1. **StarShip Inventory Adapters**:
   - `starship-client-api/` (REST API client) — implements `StarShipInventoryService` via external SWAPI API
   - `starship-client-mock/` (Mock) — implements `StarShipInventoryService` with hardcoded data
   - `starship-model/` — External API models (`SwapiStarShip`, `SwapiResponse`)

2. **Fleet Persistence Adapters**:
   - `fleets-inmemory/` — implements `FleetsService` with in-memory storage
   - `fleets-persistence/` — implements `FleetsService` with MongoDB persistence

## Project References

- **Architecture Decision**: See [README.md](../../README.md) for full DOJO explanation, SOLID principles, DDD concepts
- **Good Architecture Example**: `rescue-mission-good-architecture/` — Reference implementation with all layers correctly separated
- **Bad Architecture Example**: `rescue-mission-bad-architecture/` — Monolithic anti-pattern for comparison
- **ArchUnit Tests**: `rescue-application/src/test/java/moon/mission/rescue/HexagonalArchitectureTest.java` — Automated validation of layer separation and framework independence

---

## DOMAIN LAYER REQUIREMENTS (STRICT)

### 1. Package Structure

Domain code resides **exclusively** in `moon.mission.rescue.domain.**` packages:

- `moon.mission.rescue.domain.service.*`: Port interfaces and core business services
- `moon.mission.rescue.domain.model.*`: Entities, value objects, and domain models
- `moon.mission.rescue.rescue-domain/`: Maven module housing domain code

### 2. Framework Independence (MANDATORY)

Domain classes **absolutely cannot**:

- Import Spring Framework classes (`@Service`, `@Component`, `@Bean`, `@Autowired`, `@Configuration`, etc.)
- Depend on JPA/Hibernate annotations (`@Entity`, `@Repository`, `@Table`, etc.)
- Use database drivers, ORM frameworks, or HTTP clients directly
- Import external libraries (Jackson, Feign, RestTemplate, etc.)
- Contain any technical infrastructure concerns

**Enforcement**: The architectural test `HexagonalArchitectureTest.domainShouldNotDependOnSpring` validates this rule.

### 3. Port Definition (Interface Contracts)

Ports define the boundaries between domain and adapters. All external interactions must be declared as port interfaces in the domain:

**Rules for Ports**:

- Defined as `public interface` in `moon.mission.rescue.domain.service.*`
- Represent outbound dependencies (what the domain needs from the outside world)
- Use domain models in method signatures, **never** adapter-specific DTOs or external API models
- Include clear, minimal, business-focused method contracts
- Each port represents one specific capability (Single Responsibility)

**Example**:
```java
package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.StarShip;
import java.util.List;

/**
 * Port: Defines how the domain accesses the starship inventory.
 * Implementation may vary: REST API, database, mock, etc.
 */
public interface StarShipInventoryService {
    List<StarShip> starShips();
}
```

### 4. Domain Services (Business Logic)

Domain services encapsulate business logic and orchestrate domain models:

**Rules**:

- Depend **only** on port interfaces (abstractions), never on adapters or frameworks
- Receive port dependencies via constructor injection (no setter injection)
- Contain pure business logic with no side effects or infrastructure concerns
- Use domain models exclusively; do not reference adapter classes
- No annotations except documentation comments

**Example**:
```java
package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import java.util.List;

public class FleetAssemblerService {
    private final StarShipInventoryService starshipsInventory;
    private final FleetsService fleets;

    // Constructor injection only
    public FleetAssemblerService(
            StarShipInventoryService starshipsInventory,
            FleetsService fleets) {
        this.starshipsInventory = starshipsInventory;
        this.fleets = fleets;
    }

    // Pure business logic
    public Fleet forPassengers(int numberOfPassengers) {
        List<StarShip> candidates = starshipsInventory.starShips();
        List<StarShip> selected = selectBestFit(candidates, numberOfPassengers);
        return fleets.save(new Fleet(selected));
    }

    private List<StarShip> selectBestFit(List<StarShip> ships, int needed) {
        // Domain business rules here, no framework code
        return ships.stream()
                .filter(s -> s.cargoCapacity() >= 100000L)
                .sorted(Comparator.comparingInt(StarShip::passengersCapacity))
                .limit(calculateRequiredShips(ships, needed))
                .toList();
    }

    private int calculateRequiredShips(List<StarShip> ships, int passengers) {
        // Pure calculation, no infrastructure
        int needed = 0;
        int capacity = 0;
        for (StarShip ship : ships) {
            capacity += ship.passengersCapacity();
            needed++;
            if (capacity >= passengers) break;
        }
        return needed;
    }
}
```

### 5. Domain Models (Entities & Value Objects)

Domain models represent core business concepts:

**Rules**:

- Placed in `moon.mission.rescue.domain.model.*` (or `moon.mission.rescue.rescue-model/` Maven module)
- Must be **immutable** (use Java records, final classes, or builders)
- Contain only business logic relevant to the entity (methods that enforce business rules)
- No getters/setters—expose business-specific methods instead
- Cannot import framework classes or adapter code
- No ORM annotations; persistence mappings belong in adapters only

**Example**:
```java
package moon.mission.rescue.domain.model;

import java.util.List;
import java.util.UUID;

public record StarShip(String name, int passengersCapacity, long cargoCapacity) {
    public boolean canAccommodate(int passengers) {
        return passengersCapacity >= passengers;
    }

    public boolean hasMinimumCargoCapacity() {
        return cargoCapacity >= 100000L;
    }
}

public record Fleet(String id, List<StarShip> starShips) {
    public Fleet(List<StarShip> starShips) {
        this(UUID.randomUUID().toString(), starShips);
    }

    public int totalPassengerCapacity() {
        return starShips.stream()
                .mapToInt(StarShip::passengersCapacity)
                .sum();
    }
}
```

---

## APPLICATION LAYER REQUIREMENTS

### 1. Package Structure

Application code resides in `moon.mission.rescue.application.**` packages:

- `moon.mission.rescue.application.adapter.*`: Primary adapters (REST controllers, CLI handlers, etc.)
- `moon.mission.rescue.application.config.*`: Dependency injection configuration
- `moon.mission.rescue.application.resource.*`: Response DTOs/Resources (not domain models)
- `moon.mission.rescue.application.request.*`: Request DTOs (framework-specific)

### 2. Dependency Configuration (IoC Container)

The application layer is responsible for assembling the object graph and injecting dependencies:

**Rules**:

- Use Spring `@Configuration` classes to define `@Bean` methods
- Beans instantiate domain services and inject port dependencies
- Domain services are never directly instantiated by adapters—always use injected beans
- Ports are declared as bean types, never concrete implementations (except where explicitly chosen via configuration)

**Example**:
```java
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
```

### 3. Primary Adapters (Inbound)

Primary adapters (controllers, event handlers) receive external requests and delegate to domain services:

**Rules**:

- Annotated with `@RestController`, `@Controller`, etc.
- Inject domain services and port interfaces
- Convert incoming external formats (JSON, form data) to domain models
- Call domain services with domain models
- Convert domain responses to API-specific resources (DTOs)
- Contain no business logic—only orchestration and format conversion

**Example**:
```java
package moon.mission.rescue.application.adapter;

import moon.mission.rescue.application.request.RescueFleetRequest;
import moon.mission.rescue.application.resource.FleetResource;
import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.service.FleetAssemblerService;
import moon.mission.rescue.domain.service.FleetsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rescue-mission")
public class RescueFleetController {

    private final FleetAssemblerService fleetAssemblerService;
    private final FleetsService fleets;

    public RescueFleetController(
            FleetAssemblerService fleetAssemblerService,
            FleetsService fleets) {
        this.fleetAssemblerService = fleetAssemblerService;
        this.fleets = fleets;
    }

    @PostMapping
    public FleetResource assembleFleet(@RequestBody RescueFleetRequest request) {
        // Convert request to domain call
        Fleet fleet = fleetAssemblerService.forPassengers(request.numberOfPassengers);
        // Convert domain response to API resource
        return new FleetResource(fleet);
    }

    @GetMapping("/{id}")
    public FleetResource getFleetById(@PathVariable String id) {
        Fleet fleet = fleets.getById(id);
        return new FleetResource(fleet);
    }
}
```

---

## ADAPTER LAYER REQUIREMENTS

### 1. Package Structure

Each adapter module is independent and implements specific ports:

- `fleets-inmemory/`: In-memory implementation of `FleetsService` port
- `fleets-persistence/`: Database persistence implementation of `FleetsService` port
- `starship-client-api/`: REST API implementation of `StarShipInventoryService` port
- `starship-client-mock/`: Mock implementation of `StarShipInventoryService` port

### 2. Port Implementation (Secondary Adapters)

Adapters implement domain port interfaces to bridge the domain and external systems:

**Rules**:

- Each adapter class implements exactly one domain port interface
- Adapters can use any technology (databases, REST clients, message queues, etc.)
- External API models/DTOs are kept within adapter modules, never exposed to domain
- Adapters map between external protocols and domain models
- Annotated with `@Service` or configured as Spring beans
- Can depend on domain models and port interfaces, but domain must **never** depend on adapters

**Example (In-Memory Adapter)**:
```java
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
```

**Example (REST API Adapter)**:
```java
package moon.mission.rescue.starship.client;

import moon.mission.rescue.domain.service.StarShipInventoryService;
import moon.mission.rescue.domain.model.StarShip;
import moon.mission.rescue.starship.model.SwapiStarShip;
import moon.mission.rescue.starship.model.SwapiResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.List;

@Service
public class StarShipInventoryApi implements StarShipInventoryService {
    private final RestTemplate restTemplate;

    public StarShipInventoryApi(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<StarShip> starShips() {
        // External API call (technology-specific code here)
        SwapiResponse response = restTemplate.getForObject(
                "https://swapi.dev/api/starships/",
                SwapiResponse.class);

        // Map external model to domain model
        return response.results().stream()
                .map(this::mapToDomain)
                .toList();
    }

    private StarShip mapToDomain(SwapiStarShip external) {
        return new StarShip(
                external.name(),
                parseCapacity(external.passengers()),
                parseCapacity(external.cargoCapacity())
        );
    }

    private long parseCapacity(String capacity) {
        // Null-safe parsing from external API data
        return capacity == null || capacity.equals("unknown") 
            ? 0L 
            : Long.parseLong(capacity);
    }
}
```

### 3. External Model Isolation

External API/database models stay within adapter modules:

**Rules**:

- External models (e.g., `SwapiStarShip`, `FleetDocument`) must not leak into the domain
- Adapters are responsible for mapping external models to domain models
- Domain models are never annotated with JPA, MongoDB, Jackson annotations, etc.
- Mapping logic belongs in the adapter that uses it

**Example (External Model in Adapter)**:
```java
package moon.mission.rescue.starship.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SwapiStarShip(
    @JsonProperty("name") String name,
    @JsonProperty("passengers") String passengers,
    @JsonProperty("cargo_capacity") String cargoCapacity
) {}
```

---

## STRICT ARCHITECTURAL RULES

### Dependency Direction (Non-Negotiable)

```
Domain Core
    ↑
    | (depends on)
    |
Port Interfaces (in Domain)
    ↑
    | (implements)
    |
Application & Adapter Layers
    |
    | (depend on)
    ↓
External Frameworks & Libraries
```

**Violations that must never occur**:

1. ❌ Domain importing adapter classes
2. ❌ Domain importing application layer classes
3. ❌ Domain importing Spring, JPA, or any external framework
4. ❌ Adapter directly calling another adapter (must go through domain ports)
5. ❌ Application layer bypassing domain services to call ports directly
6. ❌ Circular dependencies between modules

### Module Isolation

Each adapter module is independent:

- **fleets-inmemory** and **fleets-persistence** both implement `FleetsService` port
- Only one can be active at runtime (configured in Spring)
- Code from one adapter module **never** imports code from another adapter module
- Swapping adapters should require only configuration changes, never domain changes

---

## TESTING REQUIREMENTS

### 1. Domain Testing

Domain logic is tested independently of adapters:

**Rules**:

- Test domain services with injected mock/stub ports
- Use simple mock implementations for ports (in-memory maps, null implementations)
- No use of Spring Test or framework testing utilities
- Domain tests validate pure business logic only
- Test files placed in `rescue-domain/src/test/`

**Example**:
```java
package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.model.Fleet;
import moon.mission.rescue.domain.model.StarShip;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FleetAssemblerServiceTest {

    // Simple stub implementation for testing
    static class InMemoryStarShipInventory implements StarShipInventoryService {
        private final List<StarShip> ships;
        InMemoryStarShipInventory(List<StarShip> ships) { this.ships = ships; }
        @Override public List<StarShip> starShips() { return ships; }
    }

    static class InMemoryFleets implements FleetsService {
        private final Map<String, Fleet> fleets = new HashMap<>();
        @Override public Fleet save(Fleet f) { fleets.put(f.id(), f); return f; }
        @Override public Fleet getById(String id) { return fleets.get(id); }
    }

    @Test
    public void shouldAssembleFleetWithMinimumNumberOfShips() {
        // Arrange
        var ships = List.of(
                new StarShip("Falcon", 100, 100000),
                new StarShip("X-Wing", 50, 100000)
        );
        var service = new FleetAssemblerService(
                new InMemoryStarShipInventory(ships),
                new InMemoryFleets()
        );

        // Act
        Fleet result = service.forPassengers(120);

        // Assert
        assertEquals(2, result.starShips().size());
        assertEquals(150, result.totalPassengerCapacity());
    }
}
```

### 2. Architectural Tests (ArchUnit)

Enforce structural rules automatically:

**File**: `HexagonalArchitectureTest.java`

```java
@AnalyzeClasses(packages = "moon.mission.rescue", importOptions = ImportOption.DoNotIncludeTests.class)
public class HexagonalArchitectureTest {

    @ArchTest
    ArchRule hexagonal = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Domain").definedBy("moon.mission.rescue.domain..")
            .layer("Application").definedBy("moon.mission.rescue.application..")
            .layer("Fleet Adapter").definedBy("moon.mission.rescue.fleet..")
            .layer("StarShip Adapter").definedBy("moon.mission.rescue.starship..")
            .whereLayer("Application").mayNotBeAccessedByAnyLayer()
            .whereLayer("Application").mayOnlyAccessLayers("Domain")
            .whereLayer("Fleet Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("StarShip Adapter").mayNotBeAccessedByAnyLayer()
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Fleet Adapter", "StarShip Adapter")
            .as("Hexagonal Architecture");

    @ArchTest
    ArchRule domainShouldNotDependOnSpring = classes()
            .that().resideInAPackage("moon.mission.rescue.domain..")
            .should().notBeMetaAnnotatedWith(Component.class)
            .andShould().notBeAnnotatedWith(Service.class)
            .as("Domain should not depend on Spring");
}
```

---

## CODE MODIFICATION CHECKLIST

Before implementing any code change, verify:

### For New Domain Classes
- [ ] Resides in `moon.mission.rescue.domain.**`
- [ ] No Spring or framework annotations
- [ ] No dependencies on adapters or external libraries
- [ ] If a service, depends only on port interfaces
- [ ] If a model, is immutable (record or final class)

### For New Adapters
- [ ] Implements a domain port interface
- [ ] All external technology details contained within adapter
- [ ] Maps external models/protocols to domain models
- [ ] No domain code imports this adapter
- [ ] Can be swapped with another adapter implementing same port

### For New Ports
- [ ] Defined as interface in `moon.mission.rescue.domain.service.*`
- [ ] Uses domain models in signatures, not DTOs
- [ ] Business-focused, not technology-focused
- [ ] Single Responsibility (represents one capability)

### For Configuration Changes
- [ ] Done in `moon.mission.rescue.application.config.*`
- [ ] Domain services are beans, not adapters
- [ ] Port interfaces are injected, not implementations
- [ ] No circular dependencies created

---

## ANTI-PATTERNS (ABSOLUTELY FORBIDDEN)

1. **Domain Imports Adapter**: ❌ Never put adapter logic in domain
   ```java
   // FORBIDDEN in domain
   import moon.mission.rescue.fleet.InMemoryFleetsService;
   ```

2. **Framework in Domain**: ❌ No Spring, JPA, or external framework annotations in domain
   ```java
   // FORBIDDEN in domain
   @Service
   @Transactional
   public class FleetAssemblerService { }
   ```

3. **DTO Leakage**: ❌ Don't use adapter-specific models in domain
   ```java
   // FORBIDDEN in domain
   public Fleet assembleFleetsFromSwapiResponse(SwapiResponse response) { }
   ```

4. **Circular Dependencies**: ❌ Application → Domain → Adapter → Application
   ```
   This creates tightly coupled layers and defeats the purpose of hexagonal architecture.
   ```

5. **Bloated Adapters**: ❌ Multiple ports per adapter
   ```java
   // FORBIDDEN
   @Service
   public class AllInOneAdapter implements FleetsService, StarShipInventoryService { }
   ```

6. **Setter Injection in Domain**: ❌ Use constructor injection only
   ```java
   // FORBIDDEN in domain
   @Autowired
   public void setFleets(FleetsService fleets) { }
   ```

7. **Direct Adapter Instantiation**: ❌ Use dependency injection via configuration
   ```java
   // FORBIDDEN
   FleetsService fs = new InMemoryFleetsService();
   ```

---

## IMPLEMENTATION STRATEGY FOR CODE SUGGESTIONS

When suggesting code modifications or new features:

1. **Identify the layer**: Is this domain logic, adapter, or application code?
2. **Check dependencies**: Does it respect the direction (inward)?
3. **Define port if needed**: New external interaction? Create port interface first.
4. **Implement adapter**: House technology details in adapter, domain stays pure.
5. **Update configuration**: Wire new beans in application configuration.
6. **Write domain test**: Test logic independently of adapters.
7. **Verify architecture**: Ensure ArchUnit tests still pass.

---

## References

- **Project DOJO**: [DOJO README](./README.md) - Full hexagonal architecture explanation
- **ArchUnit Tests**: `rescue-application/src/test/java/moon/mission/rescue/HexagonalArchitectureTest.java` - Automated architecture validation
- **Good Architecture Example**: `rescue-mission-good-architecture/` - Reference implementation
- **Bad Architecture Example**: `rescue-mission-bad-architecture/` - Anti-pattern reference (monolithic)

---

## Summary

This is a **strict hexagonal architecture project**. Every code change must:

✅ Keep the domain layer independent and framework-free  
✅ Respect dependency inversion (high-level depends on abstractions, not implementations)  
✅ Define ports for external interactions  
✅ Implement adapters to handle framework-specific concerns  
✅ Configure dependency injection in the application layer  
✅ Maintain testability by isolating domain logic  
✅ Pass architectural validation tests (ArchUnit)  

**When in doubt, refer to the working examples in `rescue-mission-good-architecture/` and consult the project README.**
