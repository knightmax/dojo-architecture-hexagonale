# Domain Independence Evaluation Criteria

## Overview
This evaluation checks whether the domain layer is completely free from framework and external technology dependencies.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- No imports from `org.springframework.*` packages
- No imports from `javax.persistence.*` or `jakarta.persistence.*`
- No imports from `com.fasterxml.jackson.*`
- No HTTP client libraries (RestTemplate, WebClient, Feign)
- No database drivers or ORM frameworks
- Domain classes contain only business logic
- Pure Java with standard library only
- All external interactions declared as port interfaces

### ❌ BAD Signs (Decrease Score)
- `@Service`, `@Component`, `@Repository`, `@Configuration` annotations
- `@Autowired`, `@Inject`, `@Value` field injection
- `@Entity`, `@Table`, `@Column`, `@Id` JPA annotations
- `@Transactional`, `@Cacheable` Spring annotations
- `@JsonProperty`, `@XmlElement` serialization annotations
- Direct HTTP client calls (RestTemplate, WebClient)
- Database query methods in domain
- Dependency on external libraries directly
- Spring configuration classes in domain
- Mock objects used in domain code
- Test annotations in production code

## Inspection Points

### 1. Domain Package Structure
```
GOOD:
moon.mission.rescue.domain.service.*
moon.mission.rescue.domain.model.*

BAD:
moon.mission.rescue.domain.adapter.*
moon.mission.rescue.domain.config.*
```

### 2. Service Class Pattern
```java
// GOOD
public class FleetAssemblerService {
    private final StarShipInventoryService inventory;
    private final FleetsService fleets;
    
    public FleetAssemblerService(...) { ... }
    public Fleet forPassengers(int count) { ... }
}

// BAD
@Service
public class FleetAssemblerService {
    @Autowired
    private StarShipInventoryService inventory;
    
    @Transactional
    public Fleet forPassengers(int count) { ... }
}
```

### 3. Model Class Pattern
```java
// GOOD
public record StarShip(String name, int capacity, long cargo) {
    public boolean canAccommodate(int passengers) { ... }
}

// BAD
@Entity
@Table(name = "starships")
public class StarShip {
    @Id @GeneratedValue
    private Long id;
    
    @Column(name = "name")
    @JsonProperty("ship_name")
    private String name;
    
    @Transient
    private transient List<Passenger> passengers;
}
```

### 4. Import Analysis
For each Java file in `domain/**`:
- Count imports from forbidden packages
- Flag any Spring annotations
- Check for external library usage
- Verify no adapter/application layer imports

### 5. Dependency Analysis
```
GOOD:
domain -> domain.service (port interfaces)
domain -> domain.model (entities/values)

BAD:
domain -> moon.mission.rescue.fleet (adapter package)
domain -> moon.mission.rescue.application (application layer)
domain -> org.springframework (framework)
domain -> com.mongodb (external library)
```

## Scoring Adjustments

### +10% each
- Remove Spring annotations from a domain class
- Eliminate ORM imports from domain
- Convert mutable entity to immutable record

### -5% each
- Discover `@Service` in domain
- Find `@Entity` annotation
- Detect external library import (non-standard)

### -10% each
- Find `@Autowired` injection in domain
- Discover `@Transactional` in domain
- Find circular dependency to adapter

### -20% each
- Domain importing application layer
- Domain directly using adapters
- Heavy framework dependency (> 30% of classes affected)

## Testing for Independence

```bash
# Should find nothing in domain
grep -r "@Service\|@Component\|@Entity\|@Autowired" src/main/java/domain/

# Should find only standard library and domain packages
grep -r "^import" src/main/java/domain/ | grep -v "^import java\|^import domain"
```

## ArchUnit Validation

```java
@ArchTest
ArchRule domainIndependent = classes()
    .that().resideInAPackage("domain..")
    .should().notDependOnClassesThat()
    .resideInAnyPackage(
        "org.springframework..",
        "javax.persistence..",
        "jakarta.persistence..",
        "com.fasterxml.jackson.."
    );
```

## Common Violations & Fixes

### Violation: @Service in Domain Service
```java
// BAD
@Service
public class FleetAssemblerService { }

// GOOD
public class FleetAssemblerService { }
```
**Action**: Remove `@Service`, move annotation to adapter wrapper if needed.

### Violation: ORM Annotations on Models
```java
// BAD
@Entity
public class Fleet { }

// GOOD
public record Fleet(String id, List<StarShip> ships) { }
```
**Action**: Move to separate `FleetDocument` in adapter, domain uses pure record.

### Violation: Setter Injection
```java
// BAD
public class FleetAssemblerService {
    @Autowired
    private StarShipService service;
}

// GOOD
public class FleetAssemblerService {
    private final StarShipService service;
    
    public FleetAssemblerService(StarShipService service) {
        this.service = service;
    }
}
```
**Action**: Convert to constructor injection.
