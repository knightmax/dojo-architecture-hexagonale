# Testability & Test Isolation Evaluation Criteria

## Overview
Domain logic must be testable independently of adapters and frameworks. Tests should run without Spring, databases, or HTTP clients. Simple mock implementations should be sufficient.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- Domain unit tests use pure mocks/stubs
- No `@SpringBootTest`, `@DataJpaTest`, or framework test annotations
- Tests run without IoC container initialization
- Mock adapters implemented as simple in-memory classes
- Fast test execution (unit tests under 100ms)
- Test dependencies on domain only
- Clear test-domain service separation
- Tests validate business logic, not framework integration
- Adapter integration tests separate from domain tests

### ❌ BAD Signs (Decrease Score)
- `@SpringBootTest` needed to test domain
- Domain tests depending on real adapters
- Database required for unit tests
- HTTP client mocking via frameworks
- Slow test suite (> 1s per test)
- Framework-specific test annotations
- Testing implementation details instead of behavior
- Monolithic integration tests
- No unit/domain tests, only integration tests

## Inspection Points

### 1. Domain Test Pattern
```java
// GOOD: Pure unit test, no framework
package moon.mission.rescue.domain.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FleetAssemblerServiceTest {
    
    // Simple stub, no framework
    static class InMemoryStarShips implements StarShipInventoryService {
        private final List<StarShip> ships;
        public InMemoryStarShips(List<StarShip> ships) { this.ships = ships; }
        @Override
        public List<StarShip> starShips() { return ships; }
    }
    
    static class InMemoryFleets implements FleetsService {
        private final Map<String, Fleet> store = new HashMap<>();
        @Override
        public Fleet save(Fleet f) { store.put(f.id(), f); return f; }
        @Override
        public Fleet getById(String id) { return store.get(id); }
    }
    
    @Test
    public void shouldAssembleFleetForPassengers() {
        // Arrange: inject simple mocks
        var ships = List.of(
            new StarShip("Falcon", 100, 100000L),
            new StarShip("X-Wing", 50, 100000L)
        );
        var service = new FleetAssemblerService(
            new InMemoryStarShips(ships),
            new InMemoryFleets()
        );
        
        // Act
        Fleet fleet = service.forPassengers(120);
        
        // Assert
        assertEquals(2, fleet.ships().size());
        assertTrue(fleet.totalCapacity() >= 120);
    }
}

// BAD: Framework-dependent test
@SpringBootTest
public class FleetAssemblerServiceTest {
    @Autowired
    private FleetAssemblerService service;
    
    @Autowired
    private FleetsService fleets; // Real adapter!
    
    @Test
    public void shouldAssembleFleet() {
        // Tests real database, HTTP calls, etc.!
        Fleet fleet = service.forPassengers(120);
        // ...
    }
}
```

### 2. Test Location Verification
```
GOOD:
domain/src/test/java/
├── moon/mission/rescue/domain/service/
│   ├── FleetAssemblerServiceTest.java
│   └── FleetsServiceStubImplTest.java (test doubles)
└── moon/mission/rescue/domain/model/
    └── FleetTest.java

BAD:
application/src/test/java/ (testing domain here)
adapter/starship-client-api/src/test/java/ (domain tests in adapter)
```

### 3. Mock Implementation Pattern
```java
// GOOD: Simple, synchronous in-memory mock
static class InMemoryInventory implements StarShipInventoryService {
    private List<StarShip> ships;
    
    public InMemoryInventory(List<StarShip> ships) {
        this.ships = new ArrayList<>(ships);
    }
    
    @Override
    public List<StarShip> starShips() {
        return new ArrayList<>(ships);
    }
}

// BAD: Framework-dependent mock
@MockBean
private StarShipInventoryService inventory;

or

private StarShipInventoryService inventory = mock(StarShipInventoryService.class);
when(inventory.starShips()).thenReturn(List.of(...));
```

### 4. Test Dependency Chart
```
GOOD (Inward only):
application tests -> domain services -> ports (mocked)
adapter tests -> domain models + ports implemented
domain tests -> domain models + port stubs

BAD (Outward):
domain tests -> application layer (tests framework)
domain tests -> adapters (tests real implementations)
domain tests -> RestTemplate (tests HTTP client)
domain tests -> Database (requires DB for unit tests)
```

### 5. Execution Time Check
```
GOOD (Fast unit tests):
FleetAssemblerServiceTest: 45ms
FleetTest: 12ms
ModelValidationTest: 30ms
Total domain: ~200ms

BAD (Slow tests):
FleetAssemblerServiceTest: 3500ms (Spring init)
FleetTest: 2100ms (DB connection)
Total domain: > 30s
```

### 6. Test Coverage
```
GOOD: Comprehensive domain coverage
✓ Domain services: > 80% coverage
✓ Models: > 90% coverage
✓ Business rules: extensively tested

BAD: Minimal domain tests
✗ No domain unit tests
✗ Only integration tests
✗ Business logic tested implicitly
```

## Scoring Adjustments

### +5% each
- Find pure unit test with stub/mock
- Verify test runs without Spring
- Confirm fast execution (< 100ms)
- Find well-structured test double
- Verify no external dependencies needed

### -3% each
- Minor test dependency on external resources
- Slight framework coupling in test setup

### -10% each
- `@SpringBootTest` on domain test
- Real adapter dependencies in unit test
- Database required for unit test
- HTTP calls in domain test
- Framework test annotations

### -15% each
- No domain unit tests
- All tests require full Spring context
- Domain services untestable without adapters
- Database/HTTP critical for domain tests

## Test Double Checklist

For each test:
- [ ] Uses constructor injection for dependencies
- [ ] All dependencies are test doubles (stubs/mocks)
- [ ] No Spring framework in test class
- [ ] No `@Autowired`, `@Mock`, or framework annotations
- [ ] Can run standalone without context initialization
- [ ] Executes in < 100ms
- [ ] Validates business behavior, not implementation
- [ ] Clear Arrange-Act-Assert structure

## Common Violations & Fixes

### Violation: @SpringBootTest for Domain Test
```java
// BAD
@SpringBootTest
public class FleetAssemblerServiceTest {
    @Autowired
    private FleetAssemblerService service;
}

// GOOD
public class FleetAssemblerServiceTest {
    @Test
    public void shouldAssemble() {
        var service = new FleetAssemblerService(
            new InMemoryStarShips(...),
            new InMemoryFleets()
        );
        Fleet fleet = service.forPassengers(100);
        assertEquals(2, fleet.ships().size());
    }
}
```
**Action**: Remove Spring, use simple mocks.

### Violation: Domain Test Depends on Adapter
```java
// BAD
public class FleetAssemblerServiceTest {
    private FleetAssemblerService service;
    private FleetsMongoRepository mongoFleets; // Real adapter!
    
    @Test
    public void test() {
        service = new FleetAssemblerService(..., mongoFleets);
        // Test depends on MongoDB being available
    }
}

// GOOD
public class FleetAssemblerServiceTest {
    @Test
    public void test() {
        var mockFleets = new InMemoryFleets();
        var service = new FleetAssemblerService(..., mockFleets);
        // Test is self-contained
    }
}
```
**Action**: Replace real adapters with test doubles.

### Violation: Slow Test Suite
```
BAD: Unit tests take 30+ seconds (Spring initialization)
GOOD: Unit tests take < 1 second
```
**Action**: Remove framework dependencies, use fast stubs.
