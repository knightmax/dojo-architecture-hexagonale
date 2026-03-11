# Application Layer Orchestration Evaluation Criteria

## Overview
The application layer configures dependencies, defines entry points (controllers), and orchestrates domain services. It must contain NO business logic.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- All `@Configuration` beans properly defined
- Controllers delegate to domain services
- Controllers convert request DTOs to domain models
- Controllers convert domain responses to API resources
- No business logic in controllers
- Clear separation: controllers handle HTTP, services handle logic
- Application imports only domain interfaces, not adapters
- REST/HTTP concerns isolated to controllers
- Domain services injected, not instantiated

### ❌ BAD Signs (Decrease Score)
- Business logic in controllers or configuration
- Controllers calling adapters directly instead of domain services
- Controllers creating domain objects directly
- Checking business rules in controllers (if/validation)
- Missing `@Configuration` bean definitions
- Controllers instantiating adapters
- Application bypassing domain services
- Mixing concerns (HTTP and business logic)
- Duplicate business logic between controllers

## Inspection Points

### 1. Controller Pattern
```java
// GOOD: Thin controller, delegates to domain
@RestController
@RequestMapping("/rescue-mission")
public class RescueFleetController {
    private final FleetAssemblerService assembler;
    
    public RescueFleetController(
            FleetAssemblerService assembler) {
        this.assembler = assembler;
    }
    
    @PostMapping
    public FleetResource createFleet(@RequestBody RescueFleetRequest req) {
        // Convert request to domain call
        Fleet fleet = assembler.forPassengers(req.numberOfPassengers);
        // Convert domain response to resource
        return new FleetResource(fleet);
    }
}

// BAD: Fat controller with business logic
@RestController
public class RescueFleetController {
    @Autowired
    private FleetsService fleets;
    
    @PostMapping
    public FleetResource createFleet(@RequestBody RescueFleetRequest req) {
        // Business logic in controller!
        if (req.numberOfPassengers <= 0) {
            throw new BusinessException(...);
        }
        
        var selectedShips = new ArrayList<>();
        for (StarShip ship : availableShips()) {
            if (ship.capacity() >= req.numberOfPassengers) {
                selectedShips.add(ship);
            }
        }
        
        Fleet fleet = new Fleet(selectedShips);
        return new FleetResource(fleets.save(fleet));
    }
    
    private List<StarShip> availableShips() { ... }
}
```

### 2. Configuration Pattern
```java
// GOOD: Clean configuration beans
@Configuration
public class DomainConfiguration {
    @Bean
    public FleetAssemblerService fleetAssembler(
            StarShipInventoryService ships,
            FleetsService fleets) {
        return new FleetAssemblerService(ships, fleets);
    }
}

// BAD: Missing configuration or scattered beans
public class SomeController {
    @Bean // WRONG! Beans belong in @Configuration class
    public FleetAssemblerService assembler() {
        return new FleetAssemblerService(...);
    }
}
```

### 3. Dependency Direction in Application
```
GOOD:
Controller imports:
  ├── domain.service.FleetAssemblerService ✓
  ├── domain.model.Fleet ✓
  ├── application.resource.FleetResource ✓
  └── application.request.RescueFleetRequest ✓

Never imports:
  ├── adapter.* ✗
  ├── fleet.InMemoryFleetsService ✗
```

### 4. Request/Response DTOs
```java
// GOOD: DTOs separate from domain
package application.request;
public record RescueFleetRequest(int numberOfPassengers) { }

package application.resource;
public record FleetResource(String id, int capacity) {
    public FleetResource(Fleet fleet) {
        this(fleet.id(), fleet.totalCapacity());
    }
}

// BAD: Domain models as DTOs
@RestController
public class Controller {
    @PostMapping
    public Fleet createFleet(@RequestBody Fleet fleet) { // WRONG!
        return fleetService.save(fleet);
    }
}
```

### 5. Application Package Structure
```
GOOD:
application/
├── adapter/
│   └── RescueFleetController.java
├── config/
│   └── DomainConfiguration.java
├── request/
│   └── RescueFleetRequest.java
└── resource/
    └── FleetResource.java

BAD:
application/
├── RescueFleetController.java (contains business logic)
├── FleetAssemblerService.java (should be domain!)
└── FleetModel.java (should be domain!)
```

### 6. No Direct Port Access
```java
// BAD: Controller calling port directly
@RestController
public class Controller {
    private final FleetsService fleets; // Port!
    
    @PostMapping
    public void save(Fleet f) {
        fleets.save(f); // Should go through domain service!
    }
}

// GOOD: Through domain service
@RestController
public class Controller {
    private final FleetAssemblerService assembler; // Domain service!
    
    @PostMapping
    public Fleet save(RescueFleetRequest req) {
        return assembler.forPassengers(req.numberOfPassengers);
    }
}
```

## Scoring Adjustments

### +5% each
- Find clean `@Configuration` class
- Verify controller delegates to domain service
- Confirm request/response DTOs
- Find proper separation of concerns

### -3% each
- Minor validation logic in controller
- Slight coupling between controller and domain

### -10% each
- Business logic in controller
- Controller calling adapter directly
- Missing `@Configuration` bean
- DTO mixed with domain model

### -15% each
- Heavy business logic in controllers
- Controllers instantiating domain services
- Duplicate logic across multiple controllers
- Significant bypass of domain services

## Validation Checklist

For each controller:
- [ ] No business logic (only HTTP mapping)
- [ ] Injects domain services, not adapters
- [ ] Converts request DTO to domain model
- [ ] Calls domain service only
- [ ] Converts domain response to resource DTO
- [ ] Returns resource, not domain model

For each `@Configuration`:
- [ ] Declares all domain service beans
- [ ] Injects port implementations (adapters)
- [ ] No business logic in bean methods
- [ ] Clean factory pattern only

## Common Violations & Fixes

### Violation: Business Logic in Controller
```java
// BAD
@PostMapping
public void process(Request req) {
    // Validation logic
    if (!validator.isValid(req)) {
        throw new ValidationException();
    }
    
    // Business logic
    var items = filterItems(req.items);
    var total = calculateTotal(items);
    
    // Persistence
    repository.save(...);
}

// GOOD
@PostMapping
public void process(Request req) {
    var result = domainService.process(req);
    return new Resource(result);
}
```
**Action**: Move all logic to domain service.

### Violation: Controller Calls Adapter
```java
// BAD
public Controller(FleetsService adapter) {
    this.adapter = adapter;
}

// GOOD
public Controller(FleetAssemblerService service) {
    this.service = service;
}
```
**Action**: Inject domain service, not port.

### Violation: Mixed DTO and Domain Model
```java
// BAD
@PostMapping
public Fleet save(@RequestBody Fleet fleet) {
    return fleets.save(fleet);
}

// GOOD
@PostMapping
public FleetResource save(@RequestBody RescueFleetRequest req) {
    Fleet fleet = assembler.forPassengers(req.numberOfPassengers);
    return new FleetResource(fleet);
}
```
**Action**: Separate DTOs from domain models.
