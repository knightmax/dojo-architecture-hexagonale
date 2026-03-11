# Model Immutability & Purity Evaluation Criteria

## Overview
Domain models must be immutable and free from ORM, persistence, and serialization framework annotations. Models should be pure Java with business logic only.

## What to Look For

### ✅ GOOD Signs (Increase Score)
- All models defined as Java records
- Models are immutable (final fields, no setters)
- Zero JPA/ORM annotations (`@Entity`, `@Table`, etc.)
- Zero serialization annotations (`@JsonProperty`, etc.)
- Models contain only business-relevant methods
- No getters (expose business methods instead)
- Clear value object semantics
- Simple, readable model definitions
- Models independent of persistence strategy

### ❌ BAD Signs (Decrease Score)
- Models still defined as mutable classes with setters
- JPA `@Entity`, `@Table`, `@Column` annotations present
- `@JsonProperty`, `@XmlElement` serialization annotations
- Getter/setter proliferation
- Complex or unclear model intent
- Bidirectional relationships (domain shouldn't have)
- Lazy-loading or persistence annotations
- Models tightly coupled to database schema
- Callback methods (`@PrePersist`, etc.)

## Inspection Points

### 1. Model Definition Pattern
```java
// GOOD: Immutable record
public record StarShip(String name, int passengersCapacity, long cargoCapacity) {
    public boolean canAccommodate(int passengers) {
        return passengersCapacity >= passengers;
    }
    
    public boolean hasMinimumCargo() {
        return cargoCapacity >= 100000L;
    }
}

// ACCEPTABLE: Final immutable class
public final class StarShip {
    private final String name;
    private final int passengersCapacity;
    private final long cargoCapacity;
    
    public StarShip(String name, int cap, long cargo) {
        this.name = name;
        this.passengersCapacity = cap;
        this.cargoCapacity = cargo;
    }
    
    public boolean canAccommodate(int passengers) {
        return passengersCapacity >= passengers;
    }
}

// BAD: Mutable with setters
public class StarShip {
    private String name;
    private int passengersCapacity;
    private long cargoCapacity;
    
    public void setName(String n) { this.name = n; }
    public void setPassengersCapacity(int c) { this.passengersCapacity = c; }
    public String getName() { return name; }
    public int getPassengersCapacity() { return passengersCapacity; }
}
```

### 2. Framework Annotation Inspection
```
GOOD: No annotations in domain models
public record Fleet(String id, List<StarShip> ships) { }

BAD: JPA annotations
@Entity
@Table(name = "fleet")
public class Fleet {
    @Id @GeneratedValue
    private Long id;
    
    @OneToMany
    private List<StarShip> ships;
}

BAD: Serialization annotations
@JsonIgnoreProperties(ignoreUnknown = true)
public record Fleet(
    @JsonProperty("fleet_id") String id,
    @JsonDeserialize(using = StarShipDeserializer.class)
    List<StarShip> ships
) { }
```

### 3. Business Methods vs. Getters
```java
// GOOD: Business methods only
public record Fleet(String id, List<StarShip> ships) {
    public int totalPassengerCapacity() {
        return ships.stream()
            .mapToInt(StarShip::passengersCapacity)
            .sum();
    }
    
    public boolean canSafelyTransport(int passengers) {
        return totalPassengerCapacity() >= passengers;
    }
}

// BAD: Pure getters (anemic model)
public record Fleet(String id, List<StarShip> ships) {
    public String id() { return id; }  // Automatic with record
    public List<StarShip> ships() { return ships; }  // Automatic with record
}

// Domain logic elsewhere in service:
int capacity = fleet.ships().stream()
    .mapToInt(s -> s.passengersCapacity())
    .sum();
```

### 4. Persistence Annotations Check
```
Forbidden in domain models:
❌ @Entity
❌ @Table
❌ @Column
❌ @Id
❌ @GeneratedValue
❌ @OneToMany
❌ @ManyToOne
❌ @JoinColumn
❌ @Transient
❌ @PrePersist
❌ @PostLoad
❌ @Version

Allowed ONLY in adapter persistence models:
✓ FleetDocument (adapter package)
✓ StarShipEntity (adapter package)
```

### 5. Model Location Verification
```
GOOD:
domain/src/main/java/moon/mission/rescue/domain/model/
├── StarShip.java (record)
├── Fleet.java (record)
└── Captain.java (record)

BAD:
persistence/src/main/java/moon/mission/rescue/fleet/
├── FleetEntity.java (JPA entity)
└── FleetJpaPersistence.java (should be adapter!)

adapter/starship-persistence/src/main/java/
└── domain/
    └── model/
        └── StarShipEntity.java (domain models leaking into adapter locations)
```

### 6. Zero Serialization Annotations
```java
// GOOD: Clean record, Jackson handles it
public record FleetResource(String id, int capacity) {
    public FleetResource(Fleet domainFleet) {
        this(domainFleet.id(), domainFleet.totalCapacity());
    }
}

// BAD: Domain model polluted with serialization
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record Fleet(
    @JsonProperty("id")
    @JsonSerialize(using = UuidSerializer.class)
    String id,
    
    @JsonProperty("ships")
    @JsonDeserialize(using = StarShipsDeserializer.class)
    List<StarShip> ships
) { }
```

## Scoring Adjustments

### +5% each
- Verify model is Java record
- Confirm zero JPA annotations
- Find business method in model
- Verify immutable field definition
- Confirm model in domain.model package

### -3% each
- Model still uses getters (minor issue)
- Slight mutability but encapsulated

### -10% each
- Model has `@Entity` or JPA annotations
- Model has serialization annotations
- Model still mutable (setters present)
- Model in wrong package location

### -15% each
- Heavy JPA annotation pollution
- Multiple serialization concerns mixed in
- Domain models serving dual purpose (entity + DTO)
- Model tightly coupled to persistence layer

## Model Immutability Checklist

For each domain model:
- [ ] Defined as `record` or `final class`
- [ ] No setter methods
- [ ] All fields are `private final`
- [ ] Zero JPA annotations
- [ ] Zero serialization annotations
- [ ] Located in `domain.model.*` package
- [ ] Contains business logic (methods), not just data
- [ ] Can be instantiated without side effects
- [ ] Equality based on field values

## Verification Commands

```bash
# Check for JPA annotations in domain
grep -r "@Entity\|@Table\|@Column\|@Id" \
  src/main/java/moon/mission/rescue/domain/

# Check for Jackson annotations in domain
grep -r "@JsonProperty\|@JsonSerialize\|@JsonDeserialize" \
  src/main/java/moon/mission/rescue/domain/

# Check for getters in records (shouldn't be needed)
grep -r "public.*get[A-Z]" \
  src/main/java/moon/mission/rescue/domain/model/
```

## Common Violations & Fixes

### Violation: JPA @Entity in Domain
```java
// BAD
@Entity
@Table(name = "fleets")
public class Fleet {
    @Id @GeneratedValue
    private Long id;
    
    @Column(name = "ship_count")
    private int shipCount;
}

// GOOD: Domain record
public record Fleet(String id, List<StarShip> ships) {
    public int totalCapacity() { ... }
}

// And in adapter:
@Entity
@Table(name = "fleets")
public class FleetDocument {
    @Id
    private String id;
    
    @OneToMany
    private List<StarShipDocument> ships;
}
```
**Action**: Move JPA annotations to adapter model (FleetDocument).

### Violation: Serialization Annotations in Domain
```java
// BAD
public record Fleet(
    @JsonProperty("id") String id,
    @JsonProperty("ships") List<StarShip> ships
) { }

// GOOD: Let Jackson use default behavior
public record Fleet(String id, List<StarShip> ships) { }

// If needed, handle in resource DTO:
public record FleetResource(
    @JsonProperty("id") String id,
    @JsonProperty("ships") List<StarShipResource> ships
) {
    public FleetResource(Fleet fleet) {
        this(fleet.id(), fleet.ships().stream()...toList());
    }
}
```
**Action**: Remove JSON annotations from domain, use DTO in adapters.

### Violation: Anemic Model (No Business Methods)
```java
// BAD
public record Fleet(String id, List<StarShip> ships) {
    // No methods!
}

// Then in service:
int capacity = fleet.ships().stream()
    .mapToInt(s -> s.passengersCapacity())
    .sum();

// GOOD: Encapsulate business logic in model
public record Fleet(String id, List<StarShip> ships) {
    public int totalPassengerCapacity() {
        return ships.stream()
            .mapToInt(StarShip::passengersCapacity)
            .sum();
    }
}

// Then in service:
int capacity = fleet.totalPassengerCapacity();
```
**Action**: Move business logic into models.

### Violation: Getter Chains
```java
// BAD: Exposes whole object graph
public record Fleet(String id, List<StarShip> ships) {
    // Record provides: id() and ships() automatically
}

// Controller:
fleet.ships().stream()  // Exposes internals
    .map(s -> s.passengersCapacity())
    ...

// GOOD: Hide internals behind business methods
public record Fleet(String id, List<StarShip> ships) {
    public int totalPassengerCapacity() {
        return ships.stream()
            .mapToInt(StarShip::passengersCapacity)
            .sum();
    }
}

// Controller:
fleet.totalPassengerCapacity();  // Clean interface
```
**Action**: Add business methods, use them instead of accessor chains.
