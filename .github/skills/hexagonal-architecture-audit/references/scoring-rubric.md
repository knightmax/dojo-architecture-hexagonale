# Scoring Rubric for Hexagonal Architecture Audit

## Overall Structure (0-100%)

### Scoring Scale
- **90-100%**: Excellent - Nearly perfect adherence to all principles
- **70-89%**: Good - Minor violations, mostly correct implementation
- **50-69%**: Fair - Significant issues but major principles respected
- **25-49%**: Poor - Many violations, inconsistent application
- **0-24%**: Critical - Fundamental misunderstandings, severe violations

## Seven Core Evaluation Dimensions

### 1. Domain Independence (0-100%)
**Weight**: 20%

Measures whether the domain layer is completely free of framework and external technology dependencies.

**Scoring Criteria**:
- 100%: Zero Spring/JPA/Jackson imports in domain, all tests pass ArchUnit validation
- 80%: One or two minor framework imports, isolated issue
- 60%: Few framework imports, affects < 10% of domain classes
- 40%: Moderate framework pollution (10-30% of domain)
- 20%: Significant framework dependency (30-50% of domain)
- 0%: Domain heavily dependent on frameworks (> 50%)

**Red Flags**:
- `@Service`, `@Component`, `@Bean` in domain classes
- `@Entity`, `@Table`, `@Column` annotations
- `@Autowired` or `@Transactional`
- Direct imports of Spring, JPA, Jackson, external libraries

---

### 2. Port Definition & Contracts (0-100%)
**Weight**: 15%

Measures clarity, correctness, and business-focus of port interface definitions.

**Scoring Criteria**:
- 100%: All ports clearly defined in domain, business-focused, use domain models exclusively
- 80%: Ports well-defined, 1-2 minor issues with model usage
- 60%: Ports exist but some expose adapter-specific DTOs
- 40%: Ports poorly defined or missing important interfaces
- 20%: Few meaningful ports, unclear contracts
- 0%: No ports defined, direct adapter access from domain

**Red Flags**:
- Ports in application or adapter layers
- Method signatures using external API models (SwapiResponse, etc.)
- DTO models instead of domain models in signatures
- Unclear or business-irrelevant port names
- Multiple unrelated responsibilities in single port

---

### 3. Adapter Isolation & Independence (0-100%)
**Weight**: 15%

Measures whether adapters are truly independent, swappable, and contain only technology-specific code.

**Scoring Criteria**:
- 100%: All adapters independent, cleanly implement ports, technology isolated
- 80%: Mostly independent with minor cross-adapter coupling
- 60%: Some adapter interdependencies, most technology code isolated
- 40%: Significant cross-adapter dependencies or shared logic
- 20%: Adapters heavily coupled, difficult to swap
- 0%: Adapters depend on each other, monolithic structure

**Red Flags**:
- Adapter A imports code from Adapter B
- Shared utility classes between adapters (outside domain)
- Adapters implement multiple ports
- Difficult to swap one adapter for another via config
- Technology details leaked to domain or application

---

### 4. Dependency Inversion & Injection (0-100%)
**Weight**: 15%

Measures correctness of dependency injection patterns and inversion of control implementation.

**Scoring Criteria**:
- 100%: Constructor injection only, no setters, application layer wires everything
- 80%: Mostly constructor injection with 1-2 setter exceptions
- 60%: Mix of constructor and setter injection, mostly correct DI patterns
- 40%: Scattered injection patterns, some framework magic
- 20%: Significant setter injection or field injection violations
- 0%: Direct instantiation, no IoC container usage, tight coupling

**Red Flags**:
- `@Autowired` on fields or setters in domain
- Direct `new` instantiation of adapters in domain/application
- Circular dependencies between modules
- Service Locator pattern usage
- Missing `@Configuration` bean definitions

---

### 5. Application Layer Orchestration (0-100%)
**Weight**: 12%

Measures whether the application layer properly configures and orchestrates without business logic.

**Scoring Criteria**:
- 100%: Clean configuration, all beans properly defined, no business logic
- 80%: Configuration mostly clean with 1-2 minor issues
- 60%: Some business logic leakage to application layer
- 40%: Mixed concerns, significant logic in application
- 20%: Blurred boundaries between application and domain
- 0%: Application layer contains core business logic

**Red Flags**:
- Business logic in controllers or configuration classes
- Controllers calling ports directly instead of domain services
- Missing `@Configuration` or bean definitions
- Controllers instantiating adapters directly
- Application bypassing domain services

---

### 6. Testability & Test Isolation (0-100%)
**Weight**: 13%

Measures ability to test domain logic independently without framework dependencies.

**Scoring Criteria**:
- 100%: Domain tests use pure mocks, no framework, all pass independently
- 80%: Domain mostly testable, minimal framework dependency in tests
- 60%: Some domain tests require Spring setup
- 40%: Many tests require framework initialization
- 20%: Domain testing mixed with adapter/integration tests
- 0%: Cannot test domain independently, requires full framework

**Red Flags**:
- `@SpringBootTest`, `@DataJpaTest` in domain tests
- Domain services directly instantiated in tests with real adapters
- No domain unit tests
- All tests are integration tests
- Mocking requires Spring context initialization

---

### 7. Model Immutability & Purity (0-100%)
**Weight**: 10%

Measures whether domain models are immutable and free of ORM/serialization annotations.

**Scoring Criteria**:
- 100%: All domain models are records or immutable, zero ORM annotations
- 80%: Mostly immutable with 1-2 classes still mutable
- 60%: Mix of immutable and mutable models, few ORM annotations
- 40%: Multiple mutable classes, scattered ORM annotations
- 20%: Mostly mutable, significant ORM annotation presence
- 0%: JPA entities in domain, fully mutable, heavy framework coupling

**Red Flags**:
- Domain models as JPA `@Entity` classes
- Getter/setter patterns instead of immutable records
- `@JsonProperty`, `@XmlElement` annotations
- Domain models in persistence layer
- Circular entity relationships

---

## Final Score Calculation

```
Final Score = (
    (DomainIndependence × 0.20) +
    (PortDefinition × 0.15) +
    (AdapterIsolation × 0.15) +
    (DependencyInversion × 0.15) +
    (ApplicationLoayer × 0.12) +
    (Testability × 0.13) +
    (ModelImmutability × 0.10)
)
```

## Report Format

Each dimension should include:
- **Score**: X% (with 0.01% precision)
- **Status**: ✅ Excellent | ⚠️ Good | ⚠️ Fair | ❌ Poor | ❌ Critical
- **Key Findings**: 3-5 main observations
- **Violations Found**: List of specific code locations with issues
- **Recommendations**: Actionable improvements

---

## Severity Levels for Violations

- **CRITICAL**: Blocks hexagonal architecture (e.g., domain depends on Spring)
- **HIGH**: Violates core principle (e.g., adapter cross-coupling)
- **MEDIUM**: Inconsistent or suboptimal implementation
- **LOW**: Minor style or convention issues
