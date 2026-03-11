---
name: hexagonal-architecture-audit
description: "Comprehensive evaluation of Java hexagonal architecture compliance. Analyzes seven core principles: domain independence, port definition, adapter isolation, dependency injection, application orchestration, testability, and model immutability. Generates detailed report with percentage scores, violation listings, and actionable recommendations."
argument-hint: "[optional: Java project path to audit, e.g., rescue-mission-good-architecture]"
---

# Hexagonal Architecture Audit Skill

Perform a thorough audit of a Java project's compliance with hexagonal architecture (ports & adapters) principles. This skill evaluates your codebase across seven dimensions and generates a comprehensive report with scores, violations, and improvement recommendations.

## What This Skill Evaluates

The audit examines these **seven core dimensions** of hexagonal architecture:

1. **Domain Independence** (20% weight) - Domain layer must be free from framework dependencies and external concerns
2. **Ports Definition** (15% weight) - Port interfaces must be properly defined, business-focused, and implemented by adapters
3. **Adapter Isolation** (15% weight) - Adapters must be independent, swappable, and implement only one port each
4. **Dependency Inversion** (15% weight) - Dependencies flow inward; constructor injection only; no circular dependencies
5. **Application Orchestration** (12% weight) - Application layer configures dependencies and coordinates without business logic
6. **Testability** (13% weight) - Domain logic testable independently; no framework required for unit tests
7. **Model Immutability** (10% weight) - Domain models are immutable records; zero ORM/serialization annotations

**Final Score**: Weighted average of all seven dimensions, expressed as percentage (0-100%).

## How to Use This Skill

### Basic Invocation
```
/@hexagonal-architecture-audit
```
Audits current workspace root directory.

### With Project Path
```
/@hexagonal-architecture-audit /path/to/java/project
or
/@hexagonal-architecture-audit rescue-mission-good-architecture
```
Audits specific Maven multi-module project.

### What to Expect
1. Automated code scanning across all dimensions
2. Detailed findings for each principle
3. Violation listings with file locations and line numbers
4. Severity classifications (CRITICAL, HIGH, MEDIUM, LOW)
5. Overall percentage score
6. Specific recommendations for improvement

## Evaluation Methodology

### Phase 1: Codebase Analysis
The skill scans Java source files to collect:
- Package structure and module organization
- Framework/annotation usage (Spring, JPA, Jackson)
- Class relationships and dependency chains
- Test structure and patterns
- Model definitions and mutability

### Phase 2: Seven-Dimension Evaluation
For each dimension, sub-evaluation criteria are applied:
- **Domain Independence**: Scans for framework imports, forbidden annotations
- **Ports Definition**: Validates location, naming, method signatures
- **Adapter Isolation**: Analyzes implementation count, cross-adapter dependencies
- **Dependency Inversion**: Traces injection patterns, identifies concrete dependencies
- **Application Orchestration**: Examines controllers for business logic leakage
- **Testability**: Assesses test structure, mock complexity, framework coupling
- **Model Immutability**: Checks record definitions, detects ORM/serialization pollution

### Phase 3: Scoring
Each dimension is scored independently (0-100%), then weighted according to the rubric:
- Collect violations and adjustments per dimension
- Calculate dimension score = 100 - (total adjustment points)
- Apply weights to generate final score

### Phase 4: Report Generation
Results compiled into comprehensive report with:
- Executive summary (overall score, status)
- Per-dimension breakdown (scores, key findings)
- Violation catalog (file locations, severity, descriptions)
- Recommendations (specific fixes for each violation type)

## Reference Documentation

Detailed evaluation criteria for each dimension:

- **[Scoring Rubric](./references/scoring-rubric.md)** - Weighted dimensions, severity levels, calculation formula
- **[Domain Independence](./references/domain-independence.md)** - Framework purity, forbidden imports, enforcement rules
- **[Ports Definition](./references/ports-definition.md)** - Interface contracts, naming conventions, God interface detection
- **[Adapter Isolation](./references/adapter-isolation.md)** - Module separation, single port per adapter, swappability
- **[Dependency Inversion](./references/dependency-inversion.md)** - Constructor injection, interface-based dependencies, circular detection
- **[Application Layer](./references/application-layer.md)** - Configuration beans, controller patterns, business logic isolation
- **[Testability](./references/testability.md)** - Unit test isolation, mock patterns, framework decoupling
- **[Model Immutability](./references/model-immutability.md)** - Records vs. mutable classes, persistence annotation purity

## Report Structure

The audit generates a report with the following sections:

### Executive Summary
```
Hexagonal Architecture Audit Report
Project: rescue-mission-good-architecture
Evaluation Date: 2025-01-15 14:32:17

Overall Score: 92%
Status: EXCELLENT ✅

Key Findings:
• Domain layer is framework-independent with proper isolation
• All ports properly defined and implemented
• Adapter modules are independent and swappable
```

### Per-Dimension Breakdown
```
1. Domain Independence (20%): 95%
   ✅ No Spring imports in domain layer
   ✅ No JPA annotations detected
   ⚠️ Single @Deprecated annotation in legacy model (minor)

2. Ports Definition (15%): 90%
   ✅ 2 port interfaces properly defined in domain.service.*
   ⚠️ FleetsService method naming could be more business-focused
   ✅ All implementations found and validated

[... remaining dimensions ...]
```

### Violations Summary
```
CRITICAL (blocks architecture):
❌ moon.mission.rescue.application.adapter.Controller
   Line 42: Direct dependency on InMemoryFleetsService (concrete implementation)
           Should depend on FleetsService (port interface)

HIGH (violates principle):
❌ moon.mission.rescue.domain.service.FleetAssembler
   Line 15: Setter injection detected
           Should use constructor injection only

MEDIUM (suboptimal):
⚠️ moon.mission.rescue.domain.model.Fleet
   No capture of business rules for validation
```

### Recommendations
```
Top Recommendations:

1. Fix application layer dependencies
   • Application → domain.service.* (ports) ✅ CONFIRMED
   • Application → adapter.* ❌ FOUND: 1 violation
   Action: Replace InMemoryFleetsService with FleetsService interface

2. Enforce constructor injection
   • Domain services use constructor ✅ CONFIRMED
   • Controllers use constructor ⚠️ 2 instances use setter
   Action: Migrate RescueFleetController to constructor injection

[... additional recommendations ...]
```

## Severity Levels

**CRITICAL** (deduct 15%) - These violations fundamentally break hexagonal architecture:
- Domain imports adapter classes
- Domain depends on Spring framework
- Circular dependencies between layers
- Adapter calls another adapter directly
- Domain service instantiated with `new` keyword

**HIGH** (deduct 10%) - These violate core principles:
- Setter/field injection anywhere
- Implementations injected instead of interfaces
- Direct adapter instantiation in adapters
- Business logic in controllers
- Controller calls port directly instead of domain service

**MEDIUM** (deduct 5%) - These are suboptimal but workable:
- Minor business logic in application layer
- Setter injection used for optional dependencies
- God interface with multiple unrelated methods
- Domain models without business methods

**LOW** (deduct 3%) - Style and consistency issues:
- Naming inconsistencies
- Missing documentation
- Test coverage below 80%
- Slow test suite (> 1s per test)

## Interpretation Guide

### Score Ranges

| Score Range | Status | Meaning |
|-------------|--------|---------|
| **95-100%** | **Excellent** 🌟 | Architecture is pristine; excellent separation of concerns |
| **90-94%** | **Very Good** ✅ | Minor issues; architecture is sound with small refinements needed |
| **80-89%** | **Good** 👍 | Mostly correct; some violations; generally follows principles |
| **70-79%** | **Fair** ⚠️ | Significant issues; architecture has clear violations |
| **60-69%** | **Poor** ❌ | Major problems; strong re-architecture recommended |
| **<60%** | **Critical** 🔴 | Monolithic or severely flawed; requires major refactoring |

### When to Take Action

- **95%+ :** Maintain this level with code reviews
- **90-94% :** Address findings in next sprint (nice-to-have fixes)
- **80-89% :** Address findings within 2 sprints (should-do improvements)
- **70-79% :** Architecture refactoring urgent (must-do)
- **<70% :** Consider architectural redesign (critical issue)

## Common Audit Patterns

### Pattern: Good Architecture
```
Domain Independence: 95%+
Ports: 95%+
Adapters: 90%+
Dependency Direction: 95%+
Application: 95%+
Testability: 90%+
Models: 100%
→ Overall: 95%+
```

### Pattern: Leaking Framework
```
Domain Independence: 60% (Spring annotations found)
Ports: 70% (Some implementations in domain)
Dependency Direction: 70% (Adapters imported in domain)
→ Overall: 65% (Requires domain layer cleanup)
```

### Pattern: Bloated Controllers
```
Application Orchestration: 50% (Business logic in controllers)
Testability: 60% (Can't test without Spring)
Domain Independence: 90% (Domain is clean, not used)
→ Overall: 75% (Controllers need refactoring)
```

## Tips for Improving Your Score

### For Domain Independence Issues:
- Run: `grep -r "@Service\|@Component\|@Entity" src/main/java/moon/mission/rescue/domain/`
- Remove all framework annotations from domain
- Keep only `public interface` and `public record`/`public final class`

### For Port Definition Issues:
- Verify all ports are in `domain.service.*` package
- Check method signatures use domain models, not external API models
- Ensure each port has multiple implementations in different adapter modules

### For Adapter Isolation Issues:
- Each adapter module implements exactly one port
- Adapters never import from other adapter modules
- Place external API models in adapter module, never in domain

### For Dependency Injection Issues:
- Change all field `@Autowired` to constructor parameters
- Change all setter injection to constructor parameters
- Use Spring `@Configuration` classes to wire beans

### For Application Orchestration Issues:
- Move business logic from controllers to domain services
- Create request/response DTOs separate from domain models
- Controllers should only map HTTP ↔ domain models

### For Testability Issues:
- Create simple in-memory mock implementations of ports
- Remove `@SpringBootTest` from domain unit tests
- Aim for domain tests to run in < 100ms total

### For Model Immutability Issues:
- Convert all domain models to Java records
- Remove any JPA annotations from domain models
- Remove any Jackson serialization annotations from domain
- Add business methods to models (move logic from services)

## Example Audit Session

```
User: /@hexagonal-architecture-audit rescue-mission-good-architecture
AI: Scanning project: rescue-mission-good-architecture

Analyzing domain independence... ✅
Analyzing ports definition... ✅
Analyzing adapter isolation... ✅
Analyzing dependency injection... ✅
Analyzing application layer... ✅
Analyzing testability... ✅
Analyzing model immutability... ✅

Generating report...

[Report with all sections]
```

## FAQ

**Q: Can I use this on non-Maven projects?**
A: The skill is optimized for Maven multi-module structures. Single-module projects may not get full benefit.

**Q: What if my project doesn't have tests?**
A: Testability dimension will score lower. Use recommendations to implement test structure.

**Q: Can I improve score without refactoring?**
A: In most cases, no. Hexagonal architecture violations require code changes to fix.

**Q: Should I aim for 100%?**
A: 95%+ is excellent and realistic. Minor deductions (3-5%) are normal for pragmatic choices.

**Q: Can adapters have lower scores than domain?**
A: Yes. Adapters legitimately use frameworks (Spring, JPA, etc.), which appear as violations in dimension evaluation. Domain must stay pure.

## When to Use This Skill

- **New team onboarding** - Establish baseline of architecture understanding
- **Architecture review** - Validate before major refactor
- **Code review prep** - Use findings to guide review process
- **Sprint planning** - Convert audit findings into technical debt backlog items
- **Migration validation** - Confirm successful refactoring
- **Documentation** - Prove compliance with architectural standards

---

**Generated with Hexagonal Architecture Audit Skill**
Version 1.0 | Last updated: 2025-01
