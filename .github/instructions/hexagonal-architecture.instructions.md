---
name: 'Hexagonal Architecture'
description: 'Enforce strict hexagonal architecture (ports & adapters) principles: domain independence, dependency inversion, port-based abstraction, and testability. Validate all code changes for architectural compliance.'
applyTo: '**/*.java'
---

# Hexagonal Architecture Enforcement

This instruction applies to all Java code in the project. Every code change must strictly adhere to Hexagonal Architecture principles. **The agent must verify compliance before suggesting or implementing modifications.**

---

## Architecture Layers

Hexagonal Architecture (Ports & Adapters) defines three distinct, one-directional layers:

### 1. Domain Layer
- **Responsibility**: Pure business logic, completely framework-independent
- **Location**: `domain/` or `**/domain/**` package hierarchy
- **Contents**: Port interfaces (`domain.service.*`), domain services, entities, value objects
- **Dependencies**: NONE—depends only on abstractions (interfaces) defined within itself
- **NO Imports**: Spring, JPA, Hibernate, external libraries, adapters, application code

### 2. Application Layer
- **Responsibility**: Dependency injection configuration, entry points (REST controllers, CLI), orchestration
- **Location**: `application/` or `**/application/**`
- **Contents**: `@Configuration` beans, primary adapters (REST controllers), DTOs for requests/responses
- **Dependencies**: Domain services (via interfaces), port interfaces, Spring Framework
- **May NOT bypass domain**: Always delegate business logic through domain services, never call ports directly

### 3. Adapter Layers
- **Responsibility**: Technology-specific implementations of domain ports
- **Location**: Separate, independent modules (e.g., `persistence/`, `client/`, `queue/`)
- **Contents**: Implementations of domain ports, external API clients, database repositories
- **Dependencies**: Domain models and port interfaces (from domain layer), technology frameworks
- **Must NOT depend on**: Other adapters, application layer, domain core

---

## Dependency Flow (Inward Only)

```
External Frameworks & Libraries
          ↓
  Application Layer
          ↓
Port Interfaces (in Domain)
          ↓
  Domain Core Logic
```

**Central Rule**: Dependencies flow **inward** toward the domain. Domain never knows about adapters, application, or external technology.

---

## Domain Layer Rules (MANDATORY)

### Port Interfaces

Ports are the contracts between domain and adapters:

- **Define as**: `public interface` in `domain.service.*` packages
- **Represent**: Outbound dependencies (what the domain needs from outside)
- **Use domain models**: Method signatures must reference domain entities/value objects, NEVER adapter-specific DTOs
- **Single Responsibility**: Each port represents one business capability
- **No annotations**: Pure Java interfaces
- **Example contract**: `List<StarShip> findAvailable()` — simple, business-focused

### Domain Services

Services contain orchestrated business logic:

- **Depend only on ports** (interfaces), never on adapters or frameworks
- **Constructor injection only**: No setters, no `@Autowired`
- **No framework annotations**: `@Service`, `@Component`, `@Bean`, `@Transactional` are FORBIDDEN
- **Pure logic**: No side effects, no infrastructure, no database calls
- **Immutable where possible**: Use records, final fields, builders

### Domain Models (Entities & Value Objects)

Represent core business concepts:

- **Immutable**: Use Java records or final classes
- **No ORM annotations**: `@Entity`, `@Table`, `@JsonProperty` do NOT belong here
- **No getters/setters**: Expose business-specific methods instead (e.g., `canAccommodate(int users)`)
- **Business methods only**: Logic that enforces business rules lives in models
- **No framework imports**: Zero Spring, JPA, Jackson, external libraries

### Framework Independence Validation

Domain classes **absolutely cannot**:
- ❌ Import `org.springframework.*` classes
- ❌ Import `javax.persistence.*` or `jakarta.persistence.*` (JPA)
- ❌ Import `com.fasterxml.jackson.*` (JSON)
- ❌ Import adapter or application layer classes
- ❌ Depend on external libraries directly
- ❌ Contain infrastructure concerns (HTTP, database, messaging)

---

## Application Layer Rules

### Configuration

Dependency injection setup happens here:

- **Use Spring `@Configuration` classes**: Define `@Bean` methods
- **Instantiate domain services**: Inject port dependencies (interfaces, not implementations)
- **Declare porta as bean types**: When injecting, use port interfaces not concrete adapters
- **Never instantiate adapters directly**: All wiring via Spring configuration

### Primary Adapters (Inbound)

REST controllers, event handlers, CLI entry points:

- **Receive external requests**: Handle HTTP, CLI, events
- **Inject domain services**: Via constructor, never adapters
- **Convert formats**: Incoming JSON/form → domain models, domain models → response DTOs
- **Delegate to domain**: All business decisions go through domain services
- **Zero business logic**: Only orchestration and format conversion

---

## Adapter Layer Rules

### Port Implementations (Secondary Adapters)

Adapters implement domain ports to bridge external systems:

- **Implement exactly ONE port interface**: Each adapter class maps to one domain responsibility
- **Use any technology**: Databases, REST clients, message brokers, caches—all allowed here
- **Map models**: Convert external protocols/data (SwapiResponse, FleetDocument, etc.) to domain models
- **Isolate external models**: External DTOs/entities stay in adapter, never leak to domain
- **Inject framework code here**: Spring `@Service`, JPA `@Repository`, `@Configuration` belong in adapters
- **Can depend on domain**: Domain models and port interfaces are acceptable dependencies

### Adapter Independence

Each adapter is standalone:
- Cannot import code from other adapters
- Multiple adapters can implement the same port (e.g., `FleetsService` in memory vs. persistent)
- Swapping adapters requires **only configuration changes**, not domain code changes

---

## Architectural Violations (ABSOLUTELY FORBIDDEN)

### 1. Domain Imports Adapter Classes
```java
// ❌ FORBIDDEN in domain.service or domain.model
import com.example.fleet.InMemoryFleetsService;
```
**Fix**: Define a port interface in domain, implement it in adapter.

### 2. Framework Annotations in Domain
```java
// ❌ FORBIDDEN in domain
@Service
@Transactional
public class FleetAssemblerService { }
```
**Fix**: Remove all annotations from domain, add them only in adapters and application.

### 3. External Models Leak to Domain
```java
// ❌ FORBIDDEN in domain method signature
public Fleet assembleFromSwapi(SwapiResponse response) { }
```
**Fix**: Adapter does mapping; domain receives clean domain models.

### 4. Application Bypasses Domain Services
```java
// ❌ FORBIDDEN in application controller
FleetsService fs = ...; // direct port call
Fleet result = fs.save(...); // should go through domain service
```
**Fix**: Always route through domain services (`FleetAssemblerService`), not directly to ports.

### 5. Setting Injection in Domain
```java
// ❌ FORBIDDEN in domain
@Autowired
public void setFleets(FleetsService fleets) { }
```
**Fix**: Use constructor injection only.

### 6. Circular Dependencies
```
Application → Adapter → Domain → Application (WRONG)
```
**Fix**: Ensure one-directional flow: adapters → ports (domain) ← application.

### 7. Bloated Adapters
```java
// ❌ FORBIDDEN
@Service
public class AllInOneAdapter implements FleetsService, StarShipService, ArmorService { }
```
**Fix**: One adapter per port. Each responsibility in separate class.

---

## Testing Requirements

### Domain Tests (Isolated, No Framework)

Test domain logic independently with stub/mock ports:

```java
// Stub implementation for testing
static class InMemoryFleets implements FleetsService {
    private Map<String, Fleet> store = new HashMap<>();
    @Override public Fleet save(Fleet f) { store.put(f.id(), f); return f; }
    @Override public Fleet getById(String id) { return store.get(id); }
}

@Test
public void shouldAssembleFleetCorrectly() {
    // Arrange: inject mock port
    var service = new FleetAssemblerService(
        new InMemoryStarShips(List.of(...)),
        new InMemoryFleets()
    );
    // Act
    Fleet result = service.forPassengers(100);
    // Assert
    assertEquals(2, result.ships().size());
}
```

**Rules**:
- No Spring Test utilities in domain tests
- No framework initialization, no `@SpringBootTest`
- Simple, synchronous stub implementations
- Test behavior, not implementation
- Domain tests must run in isolation from adapters

### Architectural Validation

Use ArchUnit to enforce structural rules automatically:

```java
@ArchTest
ArchRule layered = layeredArchitecture()
    .layer("Domain").definedBy("**.domain..")
    .layer("Application").definedBy("**.application..")
    .layer("Adapters").definedBy("**.adapter..")
    .whereLayer("Application").mayOnlyAccessLayers("Domain")
    .whereLayer("Adapters").mayOnlyAccessLayers("Domain")
    .whereLayer("Domain").mayNotAccessAnyLayer();

@ArchTest
ArchRule frameworkFree = classes()
    .that().resideInAPackage("**.domain..")
    .should().notDependOnClassesThat()
    .resideInAnyPackage("org.springframework..", "javax.persistence..", "com.fasterxml.jackson..");
```

---

## Code Modification Checklist

Before implementing **any** change, verify:

### New Domain Classes
- [ ] Resides in `domain/**` (or `domain.service.*`, `domain.model.*`)
- [ ] Zero Spring/JPA/external framework imports
- [ ] No adapters, application, or external dependencies
- [ ] Services depend only on port interfaces
- [ ] Models are immutable (records or final classes)

### New Adapters
- [ ] Implements exactly one domain port interface
- [ ] All technology details contained within adapter
- [ ] Maps external models/protocols to domain models
- [ ] Domain never imports this adapter class
- [ ] Can be swapped via configuration without domain changes

### New Ports
- [ ] Defined as `interface` in `domain.service.*`
- [ ] Method signatures use domain models, not adapter DTOs
- [ ] Business-focused (describes capability), not technology-focused
- [ ] Single Responsibility (one business concept per port)

### Configuration Changes
- [ ] Changes in application `@Configuration` classes
- [ ] Domain services are beans (instantiated via `@Bean`)
- [ ] Ports are declared as injection types (not implementations)
- [ ] Circular dependencies are eliminated

---

## Implementation Strategy

When implementing new features or refactoring:

1. **Identify the layer**: Domain logic? Adapter? Application?
2. **Check dependency direction**: Does code flow inward toward domain?
3. **Define ports first**: New external interaction → new port interface in domain
4. **Implement adapters**: House all technology (database, HTTP, files) in adapters
5. **Configure in application**: Wire everything in `@Configuration`
6. **Test domain logic**: Write unit tests with mock ports
7. **Verify architecture**: Run ArchUnit tests to validate structure

---

## References

- **SOLID Principles**: Especially Dependency Inversion and Interface Segregation
- **Domain-Driven Design (DDD)**: Entities, value objects, ubiquitous language
- **Inversion of Control (IoC)**: Constructor injection, Spring `@Configuration`
- **Ports & Adapters**: Hexagonal architecture from Alistair Cockburn
- **ArchUnit**: Tool for validating architecture through automated tests

---

## Summary

Every code change must enforce:

✅ **Domain Independence**: Zero framework code in domain  
✅ **Dependency Inversion**: High-level logic depends on abstractions, not implementations  
✅ **Ports First**: External interactions declared as domain port interfaces  
✅ **Adapter Pattern**: Technology details isolated in adapters  
✅ **Configuration Layer**: Dependency injection setup in application layer  
✅ **Testability**: Domain logic testable with simple mocks, no framework required  
✅ **Architecture Validation**: ArchUnit tests enforce structural rules  

**When in doubt**: Apply DDD principles and SOLID rules—they underpin hexagonal architecture.
