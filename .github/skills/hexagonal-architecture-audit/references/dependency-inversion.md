# Dependency Inversion & Injection Evaluation Criteria

## Overview
Dependencies must flow inward toward the domain. High-level modules depend on abstractions (ports), not concrete implementations. Injection must use constructors only, not setters or field injection.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- Constructor injection only (final fields)
- No setter injection anywhere
- No field `@Autowired` annotations
- Domain services depend on port interfaces (abstractions)
- Adapters implement port interfaces
- Application `@Configuration` beans wire everything
- Clear unidirectional dependency flow
- No circular dependencies
- Ports are declared as injection types, not implementations

### ❌ BAD Signs (Decrease Score)
- Setter injection or field injection
- Direct adapter instantiation (`new` keyword)
- Service Locator pattern usage
- Circular or bidirectional dependencies
- Implementations injected instead of interfaces
- Adapters instantiated in domain/application
- Missing `@Configuration` bean definitions
- Factories or builders used to bypass IoC
- Static service references (Singletons)

## Inspection Points

### 1. Constructor Injection Pattern
```java
// GOOD: Constructor injection, immutable dependencies
public class FleetAssemblerService {
    private final StarShipInventoryService inventory;
    private final FleetsService fleets;
    
    public FleetAssemblerService(
            StarShipInventoryService inventory,
            FleetsService fleets) {
        this.inventory = inventory;
        this.fleets = fleets;
    }
}

// BAD: Field injection
public class FleetAssemblerService {
    @Autowired
    private StarShipInventoryService inventory;
    
    @Autowired
    private FleetsService fleets;
}

// BAD: Setter injection
public class FleetAssemblerService {
    private FleetsService fleets;
    
    @Autowired
    public void setFleets(FleetsService f) { this.fleets = f; }
}
```

### 2. Interface-Based Injection
```java
// GOOD: Depends on interface
public class Controller {
    private final FleetsService fleets; // Interface!
    
    public Controller(FleetsService fleets) {
        this.fleets = fleets; // Any implementation works
    }
}

// BAD: Depends on concrete implementation
public class Controller {
    private final InMemoryFleetsService fleets; // Concrete class!
    
    public Controller() {
        this.fleets = new InMemoryFleetsService(); // Tight coupling!
    }
}
```

### 3. Configuration Bean Wiring
```java
// GOOD: Configuration beans declare dependencies
@Configuration
public class DomainConfiguration {
    @Bean
    public FleetAssemblerService fleetAssembler(
            StarShipInventoryService starships,
            FleetsService fleets) {
        return new FleetAssemblerService(starships, fleets);
    }
}

// BAD: Direct instantiation or missing beans
public class SomeService {
    private final FleetAssemblerService service 
        = new FleetAssemblerService(...); // Bypass IoC!
}
```

### 4. Dependency Direction Check
```
GOOD (Inward flow):
application -> domain.service (port interface)
adapter -> domain.service (port interface)
neither application nor adapter -> domain directly

BAD (Outward flow):
domain -> application (VIOLATION!)
domain -> adapter (VIOLATION!)
domain -> concrete adapter class (VIOLATION!)

Circular (worst):
domain -> adapter -> application -> domain (VIOLATION!)
```

### 5. No Service Locator
```java
// BAD: Service Locator (anti-pattern)
public class FleetAssemblerService {
    public Fleet forPassengers(int count) {
        FleetsService fleets = ServiceLocator.getFleets(); // WRONG!
        return fleets.save(...);
    }
}

// GOOD: Constructor injection
public class FleetAssemblerService {
    private final FleetsService fleets;
    
    public FleetAssemblerService(FleetsService fleets) {
        this.fleets = fleets;
    }
    
    public Fleet forPassengers(int count) {
        return fleets.save(...);
    }
}
```

### 6. No Direct Instantiation
```java
// BAD: New keywords in domain/application
public class FleetAssemblerService {
    private FleetsService fleets = new InMemoryFleetsService(); // WRONG!
    private InventoryService inventory = new StarShipApi(); // WRONG!
}

// GOOD: All via constructor
public class FleetAssemblerService {
    private final FleetsService fleets;
    private final StarShipInventoryService inventory;
    
    public FleetAssemblerService(
            FleetsService fleets,
            StarShipInventoryService inventory) {
        this.fleets = fleets;
        this.inventory = inventory;
    }
}
```

## Scoring Adjustments

### +5% each
- Verify constructor injection in domain service
- Confirm no `@Autowired` on fields
- Find port interface used in injection
- Verify application has `@Configuration` beans
- Confirm circular dependency eliminated

### -5% each
- Find one setter injection instance
- Discover one field injection
- Identify suboptimal but working wiring

### -10% each
- Find direct instantiation in domain/application
- Detect implementation class in injection
- Discover missing `@Configuration` bean
- Identify light circular dependency

### -15% each
- Heavy use of setter/field injection
- Service Locator pattern usage
- Direct adapter instantiation throughout
- Significant circular dependencies

## Circular Dependency Check

```
Command to find potential circular dependencies:
grep -r "import moon.mission.rescue.domain" \
  src/main/java/moon/mission/rescue/application
  
Should find only port interfaces, never domain services or models.
```

## Common Violations & Fixes

### Violation: Field Injection
```java
// BAD
public class FleetAssemblerService {
    @Autowired
    private FleetsService fleets;
}

// GOOD
public class FleetAssemblerService {
    private final FleetsService fleets;
    
    public FleetAssemblerService(FleetsService fleets) {
        this.fleets = fleets;
    }
}
```
**Action**: Convert all field injections to constructor injection.

### Violation: Concrete Implementation Injected
```java
// BAD
public Controller(InMemoryFleetsService fleets) { ... }

// GOOD
public Controller(FleetsService fleets) { ... }
```
**Action**: Use port interface, not concrete adapter.

### Violation: Direct New Instantiation
```java
// BAD
public class Service {
    private FleetAssemblerService assembler 
        = new FleetAssemblerService(...);
}

// GOOD
@Configuration
public class Config {
    @Bean
    public Service service(FleetAssemblerService assembler) {
        return new Service(assembler);
    }
}
```
**Action**: Move to Spring configuration.

### Violation: Service Locator
```java
// BAD
FleetAssemblerService service = Context.get(FleetAssemblerService.class);

// GOOD
public class Controller {
    private final FleetAssemblerService service;
    
    public Controller(FleetAssemblerService service) {
        this.service = service;
    }
}
```
**Action**: Use constructor injection via Spring.
