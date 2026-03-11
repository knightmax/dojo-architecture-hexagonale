# Adapter Isolation & Independence Evaluation Criteria

## Overview
Adapters must be independent and swappable. Each adapter implements exactly one port and contains only technology-specific code. Adapters must NOT depend on each other.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- Each adapter in separate Maven module
- Adapter implements exactly ONE port interface
- All external technology details confined to adapter
- Adapters are completely independent (no cross-imports)
- External models/DTOs isolated in adapter only
- Easy to swap adapters via Spring configuration
- Adapter naming reflects its technology (RestClient, MongoRepository, etc.)
- Clear separation: adapter imports domain, NOT vice versa
- Multiple implementations of same port available

### ❌ BAD Signs (Decrease Score)
- Adapter imports code from another adapter
- Shared utility classes between adapters (outside domain)
- Adapter implements multiple unrelated ports
- Difficult or impossible to swap one adapter for another
- Technology details leaked to domain or application
- Adapters tightly coupled to specific business logic
- No clear separation between adapter implementations
- External models visible in domain/application layers
- Adapters depend on application configuration

## Inspection Points

### 1. Module Structure Check
```
GOOD: Independent adapter modules
.github/skills/hexagonal-architecture-audit/
├── fleets-inmemory/
│   ├── src/main/java/moon/mission/rescue/fleet/
│   └── pom.xml
├── fleets-persistence/
│   ├── src/main/java/moon/mission/rescue/fleet/
│   └── pom.xml
├── starship-client-api/
│   ├── src/main/java/moon/mission/rescue/starship/client/
│   └── pom.xml
└── starship-client-mock/
    ├── src/main/java/moon/mission/rescue/starship/client/
    └── pom.xml

BAD: Monolithic or tightly coupled
adapter/
├── AllAdapters.java (multiple ports)
└── SharedUtilities.java (shared across adapters)
```

### 2. Port Implementation Count
```
GOOD: Multiple implementations per port
StarShipInventoryService:
  ├── StarShipInventoryApi (REST client)
  ├── StarShipInventoryMock (In-memory)
  └── StarShipInventoryDatabase (JDBC)

FleetsService:
  ├── InMemoryFleetsService
  └── FleetsMongoRepository

BAD: Single implementation per port
- Only one way to implement each port
- Implementation baked into one module
- No ability to swap at configuration time
```

### 3. Cross-Adapter Dependencies
```
GOOD: No imports between adapters
fleets-inmemory/
  └── import moon.mission.rescue.domain.* (OK)
  └── import java.* (OK)

fleets-persistence/
  └── import moon.mission.rescue.domain.* (OK)
  └── import org.mongodb.* (OK)
  ❌ NOT import moon.mission.rescue.fleet.inmemory (BAD!)

BAD: Adapter importing from another adapter
starship-client-api/StarShipInventoryApi.java
  ├── import moon.mission.rescue.starship.client.mock.* (VIOLATION!)
  ├── import moon.mission.rescue.adapter.shared.* (VIOLATION!)
```

### 4. External Model Isolation
```
GOOD: External models inside adapter only
adapter/starship-client-api/
└── src/main/java/moon/mission/rescue/starship/
    ├── client/
    │   └── StarShipInventoryApi.java (implements port)
    └── model/
        ├── SwapiStarShip.java (external model)
        └── SwapiResponse.java (external model)

Domain, Application never import from model/

BAD: External models leaked
domain/
└── model/
    └── SwapiResponse.java (VIOLATION! External model in domain)

application/
└── resource/
    └── SwapiStarShipDto.java (VIOLATION! Adapter model in application)
```

### 5. Technology Confinement
```
GOOD: Technology isolated to adapter
fleets-persistence/
  └── import org.springframework.data.* (OK, only here)
  └── import org.mongodb.* (OK, only here)
  └── import com.fasterxml.jackson.json.* (OK, only here)

Domain/Application never see these imports.

BAD: Technology leaks to domain
domain/
  └── import org.mongodb.* (VIOLATION!)
  
application/
  └── import com.fasterxml.jackson.* (VIOLATION!)
```

### 6. God Adapter Check
```
GOOD: One port per adapter
InMemoryFleetsService implements FleetsService { }
StarShipInventoryApi implements StarShipInventoryService { }
AuthenticationAdapter implements AuthenticationService { }

BAD: Multiple ports in one adapter
@Service
public class DeserializerAdapter 
    implements StarShipInventoryService,
               FleetsService,
               PaymentService { }
```

## Scoring Adjustments

### +5% each
- Confirm adapter in separate module
- Verify adapter implements only ONE port
- Find alternative implementation of same port
- Verify external models isolated in adapter
- Confirm no cross-adapter imports

### -3% each
- Find overly complex adapter implementation
- Unused methods in adapter
- Unclear separation of concerns

### -10% each
- Find adapter importing from another adapter
- External model leaked to domain/application
- Shared utility between adapters (outside domain)
- Adapter implements multiple ports
- Technology details visible outside adapter

### -15% each
- Adapters tightly coupled (cannot swap)
- Only one implementation per critical port
- Domain/application imports adapter directly
- Adapter depends on application configuration

## Swappability Test

For each port, verify:
```bash
# Can we remove fleets-inmemory and use only fleets-persistence?
# Without changing:
# - domain code
# - application code  
# - Other adapter code

# Only change: application/config/DomainConfiguration.java
```

If the answer is YES to all, score +10%
If the answer is NO, score -15%

## Common Violations & Fixes

### Violation: Adapter Importing Another Adapter
```java
// BAD
// starship-client-api/StarShipInventoryApi.java
import moon.mission.rescue.starship.client.mock.*; // WRONG!

// GOOD: Each adapter is independent
// starship-client-api/StarShipInventoryApi.java
import moon.mission.rescue.domain.service.*;
import org.springframework.web.client.*;
```
**Action**: Remove dependency between adapters, use domain interfaces only.

### Violation: External Model in Domain
```java
// BAD
// domain/model/StarShip.java
import moon.mission.rescue.starship.model.SwapiStarShip.*; // WRONG!

// GOOD: Domain models only
// domain/model/StarShip.java
public record StarShip(String name, int capacity, long cargo) { }
```
**Action**: Keep external models in adapter package, adapter does mapping.

### Violation: Shared Adapter Utilities
```java
// BAD
adapter/
├── shared/
│   └── CommonLogger.java (used by multiple adapters)
└── fleets-persistence/
    └── import adapter.shared.* // VIOLATION!

// GOOD: Domain utilities reusable
domain/
└── util/
    └── Logger.java (domain utility)
```
**Action**: Move shared code to domain, or duplicate in each adapter.

### Violation: Cannot Swap Adapters
```
BAD:
fleets-inmemory/ hardcoded in application/config
Can't swap to fleets-persistence without recompile

GOOD:
Spring @Configuration selects:
@ConditionalOnProperty("storage.type=inmemory")
public InMemoryFleetsService inMemory() { }

@ConditionalOnProperty("storage.type=mongodb")
public FleetsMongoRepository mongo() { }
```
**Action**: Use Spring conditional beans or profiles for adapter selection.
