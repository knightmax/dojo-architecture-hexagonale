# Hexagonal Architecture Audit: Violation Patterns & Fixes

Quick reference guide for understanding and fixing common hexagonal architecture violations found by the audit skill.

## Violation Categories

### Tier 1: CRITICAL (Block Deployment)
**Impact**: Fundamental architecture breakdown; ❌ Do NOT deploy

#### V1.1: Domain Imports Adapter
**Pattern**: Domain class imports concrete adapter class
```java
// BAD (domain layer)
package moon.mission.rescue.domain.service;

import moon.mission.rescue.fleet.InMemoryFleetsService; // FROM ADAPTER!

public class FleetAssemblerService {
    private InMemoryFleetsService fleets;
}
```

**Detection**:
```bash
grep -r "import.*\.fleet\." src/main/java/moon/mission/rescue/domain/
grep -r "import.*\.starship\." src/main/java/moon/mission/rescue/domain/
grep -r "import.*adapter\." src/main/java/moon/mission/rescue/domain/
```

**Impact**: Cannot swap adapters; domain tightly coupled to implementation

**Fix**:
1. Identify adapter class being imported
2. Find port interface that adapter implements
3. Replace concrete import with port interface import
4. Update field declaration to use port interface type

```java
// GOOD (domain layer)
package moon.mission.rescue.domain.service;

import moon.mission.rescue.domain.service.FleetsService; // PORT INTERFACE!

public class FleetAssemblerService {
    private FleetsService fleets;
}
```

---

#### V1.2: Domain Depends on Spring Framework
**Pattern**: Springs annotations (@Service, @Component, @Autowired) in domain

```java
// BAD (domain layer)
@Service
@Transactional
public class FleetAssemblerService {
    @Autowired
    private FleetsService fleets;
}
```

**Detection**:
```bash
grep -r "@Service\|@Component\|@Bean\|@Autowired" \
  src/main/java/moon/mission/rescue/domain/
```

**Impact**: Domain cannot run in non-Spring contexts; mixed concerns

**Fix**:
1. Remove all Spring annotations from domain classes
2. Ensure constructor injection instead
3. Move bean definitions to application layer

```java
// GOOD (domain layer - pure Java)
public class FleetAssemblerService {
    private final FleetsService fleets;
    
    public FleetAssemblerService(FleetsService fleets) {
        this.fleets = fleets;
    }
}

// IN APPLICATION LAYER (application.Config)
@Configuration
public class DomainConfiguration {
    @Bean
    public FleetAssemblerService fleetAssembler(FleetsService fleets) {
        return new FleetAssemblerService(fleets);
    }
}
```

---

#### V1.3: Domain Contains JPA/ORM Annotations
**Pattern**: @Entity, @Table, @Column in domain models

```java
// BAD (domain layer)
@Entity
@Table(name = "fleet")
public class Fleet {
    @Id @GeneratedValue
    private Long id;
    
    @Column(name = "ship_count")
    private int shipCount;
}
```

**Detection**:
```bash
grep -r "@Entity\|@Table\|@Column\|@Id\|@OneToMany\|@ManyToOne" \
  src/main/java/moon/mission/rescue/domain/
```

**Impact**: Domain tied to persistence strategy; cannot switch databases

**Fix**:
1. Convert domain model to immutable record
2. Move ORM annotations to adapter model
3. Create mapping logic in adapter

```java
// GOOD (domain layer)
public record Fleet(String id, List<StarShip> ships) {
    public int totalCapacity() {
        return ships.stream()
            .mapToInt(StarShip::passengersCapacity)
            .sum();
    }
}

// IN ADAPTER (fleets-persistence)
@Entity
@Table(name = "fleet")
public class FleetDocument {
    @Id
    private String id;
    
    @OneToMany
    private List<StarShipDocument> ships;
}

@Service
public class MongoFleetsService implements FleetsService {
    private final FleetRepository repo;
    
    @Override
    public Fleet save(Fleet fleet) {
        FleetDocument doc = mapToDocument(fleet);
        repo.save(doc);
        return fleet;
    }
}
```

---

#### V1.4: Circular Dependencies Between Layers
**Pattern**: A → B → C → A dependency chain

```
EXAMPLE CYCLE:
domain.service → application.adapter (imports controller)
application.adapter → adapter.impl (imports concrete adapter)
adapter.impl → domain.model ✓ (OK, but creates circuit)

RESULT: Cannot test domain without loading entire Stack
```

**Detection**:
```bash
# Visual inspection: Check import statements in each layer
# ArchUnit: Run automated architecture tests
mvn clean test -Dtest=HexagonalArchitectureTest
```

**Impact**: Cannot test layers independently; tight coupling

**Fix**:
1. Map out all dependencies: `A depends on B?`
2. Identify the circular cycle
3. Remove problematic edge (usually adapter importing adapter)
4. Verify flow goes inward: Application → Domain ← Adapters

---

#### V1.5: Adapters Directly Call Each Other
**Pattern**: One adapter imports and uses another adapter

```java
// BAD (in adapter A)
package moon.mission.rescue.fleet;

import moon.mission.rescue.starship.StarShipInventoryApi; // ANOTHER ADAPTER!

@Service
public class FleetController {
    private StarShipInventoryApi starshipApi;
}
```

**Detection**:
```bash
grep -r "import.*\.fleet\." src/main/java/moon/mission/rescue/starship/
grep -r "import.*\.starship\." src/main/java/moon/mission/rescue/fleet/
```

**Impact**: Adapters are tightly coupled; cannot swap one independently

**Fix**:
1. Both adapters implement port interfaces in domain
2. Let domain coordinate via ports
3. Inject ports from application layer

```java
// GOOD (domain layer)
public interface FleetsService { ... }
public interface StarShipInventoryService { ... }

// GOOD (adapter A)
@Service
public class FleetController {
    private final FleetsService fleets;
    private final FleetAssemblerService assembler;
    
    public FleetController(FleetsService f, FleetAssemblerService a) {
        this.fleets = f;
        this.assembler = a;
    }
}

// GOOD (adapter B)
@Service
public class StarShipInventoryApi implements StarShipInventoryService {
    // Doesn't know about fleet adapter
}

// GOOD (domain)
public class FleetAssemblerService {
    private final StarShipInventoryService ships;
    private final FleetsService fleets;
    
    // Coordinates between the two adapters via ports
}
```

---

### Tier 2: HIGH (Major Violations)
**Impact**: Breaks principles; ⚠️ Must fix before next release

#### H2.1: Setter Injection in Services
**Pattern**: @Autowired on setter methods

```java
// BAD
public class FleetAssemblerService {
    private FleetsService fleets;
    
    @Autowired
    public void setFleets(FleetsService f) {
        this.fleets = f;
    }
}
```

**Impact**: Dependencies not immutable; harder to test; unclear requirements

**Fix**:
```java
// GOOD
public class FleetAssemblerService {
    private final FleetsService fleets;
    
    public FleetAssemblerService(FleetsService fleets) {
        this.fleets = fleets;
    }
}

// In application configuration:
@Configuration
public class Config {
    @Bean
    public FleetAssemblerService assembler(FleetsService fleets) {
        return new FleetAssemblerService(fleets);
    }
}
```

---

#### H2.2: Field Injection (@Autowired on fields)
**Pattern**: Direct field injection instead of constructor

```java
// BAD
public class Controller {
    @Autowired
    private FleetAssemblerService assembler;
    
    @PostMapping
    public void create() {
        assembler.assemble(...); // Depends on Spring initialization
    }
}
```

**Impact**: Cannot test without Spring container; hidden dependencies

**Fix**:
```java
// GOOD
@RestController
public class RescueFleetController {
    private final FleetAssemblerService assembler;
    
    public RescueFleetController(FleetAssemblerService assembler) {
        this.assembler = assembler;
    }
}
```

---

#### H2.3: Concrete Implementation Injected Instead of Interface
**Pattern**: Injecting adapter class instead of port interface

```java
// BAD
public class Controller {
    private final InMemoryFleetsService fleets; // Concrete adapter!
    
    public Controller(InMemoryFleetsService fleets) { // Couples to implementation!
        this.fleets = fleets;
    }
}
```

**Impact**: Cannot swap adapter implementations at runtime

**Fix**:
```java
// GOOD
public class Controller {
    private final FleetsService fleets; // Port interface!
    
    public Controller(FleetsService fleets) {
        this.fleets = fleets;
    }
}

// In configuration:
@Configuration
public class Config {
    @Bean
    public FleetsService fleets() {
        // Choose implementation at config time only
        return environment.isProd() 
            ? new MongoFleetsService(...)
            : new InMemoryFleetsService();
    }
}
```

---

#### H2.4: Business Logic in Controllers
**Pattern**: Validation/calculation code in REST controller

```java
// BAD
@PostMapping
public FleetResource create(@RequestBody RescueFleetRequest req) {
    // Validation logic
    if (req.numberOfPassengers <= 0) {
        throw new ValidationException(...);
    }
    
    // Business logic
    var candidates = repo.findByCapacity(req.numberOfPassengers);
    var best = selectBest(candidates);
    var fleet = new Fleet(best);
    
    // Persistence
    return new FleetResource(repo.save(fleet));
}

private List<StarShip> selectBest(List<StarShip> candidates) { ... }
```

**Impact**: Business logic scattered; hard to test; controllers too complex

**Fix**:
```java
// GOOD (controller)
@PostMapping
public FleetResource create(@RequestBody RescueFleetRequest req) {
    // Convert request to domain call
    Fleet fleet = assembler.forPassengers(req.numberOfPassengers);
    // Convert response to API resource
    return new FleetResource(fleet);
}

// GOOD (domain service)
public class FleetAssemblerService {
    private final StarShipInventoryService ships;
    private final FleetsService fleets;
    
    public Fleet forPassengers(int passengers) {
        List<StarShip> candidates = ships.starShips();
        List<StarShip> selected = selectBest(candidates, passengers);
        return fleets.save(new Fleet(selected));
    }
    
    private List<StarShip> selectBest(List<StarShip> ships, int needed) {
        // Pure business logic
    }
}
```

---

### Tier 3: MEDIUM (Suboptimal)
**Impact**: Works but violates principles; 📋 Should fix in next 1-2 sprints

#### M3.1: Port Not Located in Domain.Service Package
**Pattern**: Port interface in wrong location

```java
// BAD
package moon.mission.rescue.application;
public interface FleetsService { } // Should be in domain.service!

// OR
package moon.mission.rescue.fleet;
public interface FleetsService { } // Wrong module!
```

**Detection**:
```bash
find . -name "*.java" -exec grep -l "interface.*Service" {} \; \
  | grep -v "domain/.*service"
```

**Fix**:
- Move to `moon.mission.rescue.domain.service.*`
- Update all imports

---

#### M3.2: Port Method Signatures Use External API Models
**Pattern**: Port method returns external DTO instead of domain model

```java
// BAD
public interface StarShipInventoryService {
    SwapiResponse getStarShips(); // External API model!
}

// GOOD
public interface StarShipInventoryService {
    List<StarShip> starShips(); // Domain model!
}
```

**Fix**:
1. Create domain model (StarShip)
2. Update port method signature
3. Have adapter map external model to domain

---

#### M3.3: God Adapter (Multiple Responsibilities)
**Pattern**: One adapter implements multiple unrelated ports

```java
// BAD
@Service
public class AllInOneAdapter 
    implements FleetsService, StarShipInventoryService { }
```

**Fix**:
- Split into separate adapter classes
- One class per port

---

### Tier 4: LOW (Style Issues)
**Impact**: Doesn't break architecture but inconsistent; 💡 Nice-to-have

#### L4.1: Missing @Configuration Bean
**Pattern**: Domain service not registered as Spring bean

**Fix**:
```java
@Configuration
public class DomainConfiguration {
    @Bean
    public FleetAssemblerService assembler(
            StarShipInventoryService ships,
            FleetsService fleets) {
        return new FleetAssemblerService(ships, fleets);
    }
}
```

#### L4.2: Poor Port Naming
**Pattern**: Port name doesn't clearly indicate responsibility

```java
// UNCLEAR
public interface Service { }
public interface Manager { }

// CLEAR
public interface StarShipInventoryService { }
public interface FleetPersistenceService { }
```

#### L4.3: Models Without Business Methods
**Pattern**: Anemic models with only getters

```java
// WEAK
public record Fleet(String id, List<StarShip> ships) {}

// BETTER
public record Fleet(String id, List<StarShip> ships) {
    public int totalCapacity() {
        return ships.stream()
            .mapToInt(StarShip::passengersCapacity)
            .sum();
    }
}
```

---

## Violation Mapping to Audit Dimensions

| Violation | Primary Dimension | Secondary |
|-----------|------------------|-----------|
| V1.1 Domain Imports Adapter | Domain Independence | Adapter Isolation |
| V1.2 Domain Depends on Spring | Domain Independence | Testability |
| V1.3 Domain Contains JPA | Domain Independence | Model Immutability |
| V1.4 Circular Dependencies | Dependency Inversion | All layers |
| V1.5 Adapters Call Each Other | Adapter Isolation | Dependency Inversion |
| H2.1 Setter Injection | Dependency Inversion | Testability |
| H2.2 Field Injection | Dependency Inversion | Testability |
| H2.3 Concrete Injeced | Dependency Inversion | Adapter Isolation |
| H2.4 Business Logic in Controllers | Application Layer | Testability |
| M3.1 Port Not in Domain | Ports Definition | Domain Independence |
| M3.2 Port Uses External Model | Ports Definition | Adapter Isolation |
| M3.3 God Adapter | Adapter Isolation | Ports Definition |

---

## Quick Fix Checklist

For each violation type, follow this checklist:

### Domain Independence Violations
- [ ] Remove all Spring annotations from domain
- [ ] Remove all framework imports from domain
- [ ] Remove all ORM annotations from models
- [ ] Keep only: `public interface`, `public record`, `public final class`

### Ports Definition Violations
- [ ] Move port to `domain.service.*`
- [ ] Use domain models in signatures
- [ ] Update method names to be business-focused
- [ ] Ensure multiple implementations in different adapters

### Adapter Isolation Violations
- [ ] One adapter per module
- [ ] One port per adapter class
- [ ] No cross-adapter imports
- [ ] External models stay in adapter package

### Dependency Inversion Violations
- [ ] Convert to constructor injection
- [ ] Remove setter injection
- [ ] Remove field injection
- [ ] Inject interfaces, not implementations

### Application Layer Violations
- [ ] Move business logic to domain
- [ ] Keep controllers thin (request → domain → response)
- [ ] Create `@Configuration` beans for all services
- [ ] Create DTOs separate from domain models

### Testability Violations
- [ ] Remove `@SpringBootTest` from domain tests
- [ ] Create simple mock implementations
- [ ] Ensure tests run without framework
- [ ] Target < 100ms per domain test

### Model Immutability Violations
- [ ] Convert to Java records
- [ ] Remove all JPA annotations
- [ ] Remove all serialization annotations
- [ ] Add business methods to models

---

**Generated for Hexagonal Architecture Audit Skill**
