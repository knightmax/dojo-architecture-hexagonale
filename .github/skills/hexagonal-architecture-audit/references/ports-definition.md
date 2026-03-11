# Ports Definition Evaluation Criteria

## Overview
Ports are the contracts/interfaces that define how the domain interacts with external systems. This evaluation verifies that ports are properly defined, business-focused, and use domain models.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- Ports defined as `interface` in `domain.service.*` packages
- Method signatures use domain entities/value objects only
- Clear, business-focused naming (e.g., `StarShipInventoryService`)
- Single Responsibility Principle: each port represents ONE capability
- Minimal, lean method contracts
- No framework annotations on ports
- Ports documented with clear purpose comments
- Straightforward return types (no complex wrapping)

### ❌ BAD Signs (Decrease Score)
- Ports defined outside domain layer
- Method signatures use external API models (SwapiResponse, FleetDocument)
- Generic/unclear naming (ServiceA, Utility, Helper)
- Ports mixing multiple unrelated responsibilities
- Verbose or confusing method contracts
- Spring annotations on port interfaces
- No documentation or unclear purpose
- Ports expose internal implementation details
- Too many methods per port (God Interface)

## Inspection Points

### 1. Port Location Verification
```
GOOD:
domain/service/StarShipInventoryService.java
domain/service/FleetsService.java
domain/service/PaymentProcessorService.java

BAD:
application/adapter/StarShipInventoryAdapter.java
adapter/StarShipInventoryService.java (in adapter module root)
infrastructure/StarShipInventoryService.java
```

### 2. Port Definition Pattern
```java
// GOOD: Business-focused, uses domain models
public interface StarShipInventoryService {
    List<StarShip> starShips();
    Optional<StarShip> findById(String id);
    void reserve(StarShip ship);
}

// BAD: Exposes external models
public interface StarShipInventoryService {
    List<SwapiStarShip> getShipsFromApi();
    SwapiResponse fetchAllShips();
    void saveToDatabase(SwapiStarShip swapi);
}

// BAD: God Interface
public interface InventoryService {
    List<StarShip> ships();
    void addShip(StarShip ship);
    void removeShip(String id);
    void updatePrice(String id, BigDecimal price);
    void processPayment(Payment payment);
    void sendNotification(Notification notif);
    List<Cargo> allCargo();
    void manageWarehouse(Warehouse w);
}
```

### 3. Method Signature Analysis
```java
// GOOD: Domain models only
List<StarShip> findAvailable();
Fleet save(Fleet fleet);
Optional<Captain> findByName(String name);

// BAD: Adapter models leak into signature
SwapiResponse fetchFromExternalApi();
FleetDocument persistToMongo(Fleet fleet);
List<SwapiStarShip> getRemoteShips();
void updateWithJson(JsonNode data);
```

### 4. Port Naming Convention
```
GOOD:
- StarShipInventoryService (Clear, business domain related)
- FleetsService (Plural ok, represents a collection responsibility)
- CrewRepository (Clear persistence responsibility)
- MissionNotifier (Clear action-based naming)
- PaymentProcessor (Clear responsibility)

BAD:
- InventoryServiceAdapter (Suggests implementation, belongs in adapter)
- ServiceA, ServiceB (Meaningless)
- DatabaseHelper (Technical, not business-focused)
- Utils, Util (Too generic)
- IStarShipService (Interface notation, unnecessary in Java 8+)
```

### 5. Dependency Chain Check
```
GOOD:
domain.service.FleetAssemblerService
    -> domain.service.StarShipInventoryService (port)
    -> domain.service.FleetsService (port)
    -> domain.model.Fleet, StarShip (models)

BAD:
domain.service.FleetAssemblerService
    -> adapter.StarShipInventoryApi (concrete adapter)
    -> external.SwapiStarShip (external model)
    -> org.springframework.web.client.RestTemplate (framework)
```

### 6. Port Implementation Count
```
GOOD: Multiple independent implementations
- domain.service.StarShipInventoryService
  ├── adapter.starship.client.StarShipInventoryApi (REST)
  ├── adapter.starship.mock.StarShipInventoryMock (Mock)
  └── adapter.starship.database.StarShipRepository (DB)

BAD: No alternatives, tightly coupled
- Only one implementation
- Implementation in domain package
- Implementation inextricably linked to one technology
```

## Scoring Adjustments

### +5% each
- Find well-documented port interface
- Confirm port uses domain models exclusively
- Verify port in correct `domain.service.*` location
- Find port with single, clear responsibility
- Count implementation alternative for a port

### -3% each
- Find unused method in a port
- Discover overly complex method signature
- Port slightly unclear naming

### -10% each
- Find port using external API models in signature
- Discover port implementing multiple unrelated responsibilities
- Find port located in wrong package
- Port defined with business logic (not interface)

### -15% each
- Port in application or adapter layer
- Port heavily coupled to specific implementation
- No ports defined, direct adapter access
- Port mixing business and technical concerns

## Verification Checklist

For each port:
- [ ] Located in `domain.service.*` package
- [ ] Defined as pure Java `interface`
- [ ] All method parameters are domain models or primitives
- [ ] All return types are domain models or primitives
- [ ] Zero Spring annotations
- [ ] Clear, business-focused name
- [ ] Single Responsibility (one business capability)
- [ ] Maximum 5-8 methods (Lean Interface)
- [ ] Has at least 1 implementation in adapters
- [ ] Documentation explains its role

## Common Violations & Fixes

### Violation: Port Uses External Models
```java
// BAD
public interface StarShipService {
    SwapiResponse fetchAllShips();
    void saveToDB(SwapiStarShip ship);
}

// GOOD
public interface StarShipService {
    List<StarShip> available();
    void save(StarShip ship);
}
```
**Action**: Adapter handles mapping between external models and domain models.

### Violation: Port in Wrong Location
```
BAD:
adapter/starship-client-api/StarShipInventoryService.java

GOOD:
rescue-domain/src/main/java/moon/mission/rescue/domain/service/StarShipInventoryService.java
```
**Action**: Move interface to domain layer.

### Violation: God Interface
```java
// BAD: Too many responsibilities
public interface InventoryService {
    List<StarShip> ships();
    void addShip(StarShip ship);
    void processPayment(Payment p);
    void notifyCustomers(String msg);
}

// GOOD: Separate concerns
public interface StarShipInventory {
    List<StarShip> available();
}

public interface PaymentProcessor {
    Receipt process(Payment p);
}

public interface CustomerNotifier {
    void notify(String msg);
}
```
**Action**: Split into focused ports, each with single responsibility.

### Violation: Port Location in Application
```java
// BAD: In application layer
package moon.mission.rescue.application.adapter;
public interface StarShipService { }

// GOOD: In domain layer
package moon.mission.rescue.domain.service;
public interface StarShipService { }
```
**Action**: Move to `domain.service.*`.
