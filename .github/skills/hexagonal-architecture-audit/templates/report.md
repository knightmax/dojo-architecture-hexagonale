# Hexagonal Architecture Audit Report Template

**Evaluation Date**: {TIMESTAMP}  
**Project**: {PROJECT_NAME}  
**Auditor**: Hexagonal Architecture Audit Skill v1.0

---

## Executive Summary

| Metric | Value |
|--------|-------|
| **Overall Score** | {OVERALL_SCORE}% |
| **Status** | {STATUS_EMOJI} {STATUS_TEXT} |
| **Modules Analyzed** | {MODULE_COUNT} |
| **Classes Scanned** | {CLASS_COUNT} |
| **Critical Violations** | {CRITICAL_COUNT} |
| **High Violations** | {HIGH_COUNT} |
| **Medium Violations** | {MEDIUM_COUNT} |
| **Low Violations** | {LOW_COUNT} |

### Overall Assessment
{EXECUTIVE_SUMMARY_TEXT}

---

## Dimension Breakdown

### 1. Domain Independence: {DOMAIN_SCORE}%
**Weight**: 20% | **Status**: {DOMAIN_STATUS}

**Key Findings**:
- {DOMAIN_FINDING_1}
- {DOMAIN_FINDING_2}
- {DOMAIN_FINDING_3}

**Violations**: {DOMAIN_VIOLATION_COUNT}
{%- if DOMAIN_VIOLATIONS %}
- {DOMAIN_VIOLATION_1}
- {DOMAIN_VIOLATION_2}
{%- endif %}

**Recommendations**:
{DOMAIN_RECOMMENDATIONS}

---

### 2. Ports Definition: {PORTS_SCORE}%
**Weight**: 15% | **Status**: {PORTS_STATUS}

**Key Findings**:
- Total ports found: {PORT_COUNT}
- {PORTS_FINDING_1}
- {PORTS_FINDING_2}
- {PORTS_FINDING_3}

**Violations**: {PORTS_VIOLATION_COUNT}
{%- if PORTS_VIOLATIONS %}
- {PORTS_VIOLATION_1}
- {PORTS_VIOLATION_2}
{%- endif %}

**Recommendations**:
{PORTS_RECOMMENDATIONS}

---

### 3. Adapter Isolation: {ADAPTERS_SCORE}%
**Weight**: 15% | **Status**: {ADAPTERS_STATUS}

**Key Findings**:
- Adapter modules found: {ADAPTER_MODULE_COUNT}
- Cross-module dependencies: {CROSS_MODULE_DEPS}
- {ADAPTERS_FINDING_1}
- {ADAPTERS_FINDING_2}

**Violations**: {ADAPTERS_VIOLATION_COUNT}
{%- if ADAPTERS_VIOLATIONS %}
- {ADAPTERS_VIOLATION_1}
- {ADAPTERS_VIOLATION_2}
{%- endif %}

**Recommendations**:
{ADAPTERS_RECOMMENDATIONS}

---

### 4. Dependency Inversion: {DI_SCORE}%
**Weight**: 15% | **Status**: {DI_STATUS}

**Key Findings**:
- Constructor injections: {CONSTRUCTOR_INJECTION_COUNT}
- Setter injections: {SETTER_INJECTION_COUNT}
- Field injections: {FIELD_INJECTION_COUNT}
- {DI_FINDING_1}
- {DI_FINDING_2}

**Violations**: {DI_VIOLATION_COUNT}
{%- if DI_VIOLATIONS %}
- {DI_VIOLATION_1}
- {DI_VIOLATION_2}
{%- endif %}

**Recommendations**:
{DI_RECOMMENDATIONS}

---

### 5. Application Orchestration: {APP_SCORE}%
**Weight**: 12% | **Status**: {APP_STATUS}

**Key Findings**:
- Controllers scanned: {CONTROLLER_COUNT}
- Business logic leaks found: {LOGIC_LEAK_COUNT}
- {APP_FINDING_1}
- {APP_FINDING_2}

**Violations**: {APP_VIOLATION_COUNT}
{%- if APP_VIOLATIONS %}
- {APP_VIOLATION_1}
- {APP_VIOLATION_2}
{%- endif %}

**Recommendations**:
{APP_RECOMMENDATIONS}

---

### 6. Testability: {TEST_SCORE}%
**Weight**: 13% | **Status**: {TEST_STATUS}

**Key Findings**:
- Unit tests found: {UNIT_TEST_COUNT}
- Framework tests (@SpringBootTest): {FRAMEWORK_TEST_COUNT}
- Mock implementations: {MOCK_COUNT}
- {TEST_FINDING_1}
- {TEST_FINDING_2}

**Violations**: {TEST_VIOLATION_COUNT}
{%- if TEST_VIOLATIONS %}
- {TEST_VIOLATION_1}
- {TEST_VIOLATION_2}
{%- endif %}

**Recommendations**:
{TEST_RECOMMENDATIONS}

---

### 7. Model Immutability: {MODEL_SCORE}%
**Weight**: 10% | **Status**: {MODEL_STATUS}

**Key Findings**:
- Domain models found: {MODEL_COUNT}
- Records: {RECORD_COUNT}
- Mutable classes: {MUTABLE_COUNT}
- JPA annotations in domain: {JPA_ANNOTATION_COUNT}
- {MODEL_FINDING_1}
- {MODEL_FINDING_2}

**Violations**: {MODEL_VIOLATION_COUNT}
{%- if MODEL_VIOLATIONS %}
- {MODEL_VIOLATION_1}
- {MODEL_VIOLATION_2}
{%- endif %}

**Recommendations**:
{MODEL_RECOMMENDATIONS}

---

## Weighted Score Calculation

```
Domain Independence     × 0.20 = {DOMAIN_SCORE} × 0.20 = {DOMAIN_WEIGHTED}
Ports Definition        × 0.15 = {PORTS_SCORE} × 0.15 = {PORTS_WEIGHTED}
Adapter Isolation       × 0.15 = {ADAPTERS_SCORE} × 0.15 = {ADAPTERS_WEIGHTED}
Dependency Inversion    × 0.15 = {DI_SCORE} × 0.15 = {DI_WEIGHTED}
Application Layer       × 0.12 = {APP_SCORE} × 0.12 = {APP_WEIGHTED}
Testability             × 0.13 = {TEST_SCORE} × 0.13 = {TEST_WEIGHTED}
Model Immutability      × 0.10 = {MODEL_SCORE} × 0.10 = {MODEL_WEIGHTED}
                         ────────────────────────────────
OVERALL SCORE                  = {OVERALL_SCORE}%
```

---

## Violations Catalog

### Critical Violations 🔴
Domain layer broken, architecture fundamentally compromised.

{%- for violation in CRITICAL_VIOLATIONS %}
**{{ violation.id }}. {{ violation.title }}**
- **File**: `{{ violation.file }}:{{ violation.line }}`
- **Severity**: CRITICAL (deduct 15%)
- **Description**: {{ violation.description }}
- **Impact**: {{ violation.impact }}
- **Fix**: {{ violation.fix }}

{%- endfor %}

### High Violations ❌
Violations of core hexagonal principles.

{%- for violation in HIGH_VIOLATIONS %}
**{{ violation.id }}. {{ violation.title }}**
- **File**: `{{ violation.file }}:{{ violation.line }}`
- **Severity**: HIGH (deduct 10%)
- **Description**: {{ violation.description }}
- **Fix**: {{ violation.fix }}

{%- endfor %}

### Medium Violations ⚠️
Suboptimal patterns; principles still mostly respected.

{%- for violation in MEDIUM_VIOLATIONS %}
**{{ violation.id }}. {{ violation.title }}**
- **File**: `{{ violation.file }}:{{ violation.line }}`
- **Severity**: MEDIUM (deduct 5%)
- **Description**: {{ violation.description }}

{%- endfor %}

### Low Violations 💡
Style and consistency suggestions.

{%- for violation in LOW_VIOLATIONS %}
**{{ violation.id }}. {{ violation.title }}**
- **File**: `{{ violation.file }}:{{ violation.line }}`
- **Severity**: LOW (deduct 3%)
- **Note**: {{ violation.description }}

{%- endfor %}

---

## Top Recommendations

### Priority 1: Critical Fixes
These must be addressed for architecture to be valid:

1. {TOP_REC_1_TITLE}
   - **Impact**: {TOP_REC_1_IMPACT}
   - **Effort**: {TOP_REC_1_EFFORT}
   - **Steps**: {TOP_REC_1_STEPS}

2. {TOP_REC_2_TITLE}
   - **Impact**: {TOP_REC_2_IMPACT}
   - **Effort**: {TOP_REC_2_EFFORT}
   - **Steps**: {TOP_REC_2_STEPS}

### Priority 2: High Priority Fixes
Should be addressed in next 1-2 sprints:

1. {MID_REC_1_TITLE}
   - **Effort**: {MID_REC_1_EFFORT}

2. {MID_REC_2_TITLE}
   - **Effort**: {MID_REC_2_EFFORT}

### Priority 3: Nice-to-Have Improvements
Can be addressed as technical debt:

1. {LOW_REC_1_TITLE}
2. {LOW_REC_2_TITLE}

---

## Score Interpretation

**Overall Score: {OVERALL_SCORE}%**

| Range | Status | Interpretation |
|-------|--------|-----------------|
| 95-100% | 🌟 Excellent | Architecture is pristine |
| 90-94% | ✅ Very Good | Minor issues; sound architecture |
| 80-89% | 👍 Good | Mostly correct; refinements needed |
| 70-79% | ⚠️ Fair | Significant violations; refactoring recommended |
| 60-69% | ❌ Poor | Major problems; urgent re-architecture |
| <60% | 🔴 Critical | Monolithic; requires major redesign |

**Your project falls in: {SCORE_RANGE}**

---

## Module Analysis

{%- for module in MODULES %}
### Module: `{module.name}`
- **Status**: {module.status}
- **Violations**: {module.violation_count}
- **Key Classes**: {module.class_count}
- **Tests**: {module.test_count}

{%- endfor %}

---

## Architecture Strengths

✅ {STRENGTH_1}
✅ {STRENGTH_2}
✅ {STRENGTH_3}

---

## Architecture Weaknesses

⚠️ {WEAKNESS_1}
⚠️ {WEAKNESS_2}
⚠️ {WEAKNESS_3}

---

## Next Steps

1. **Review violations** - Prioritize by severity level
2. **Plan refactoring** - Break into manageable chunks
3. **Implement fixes** - Start with critical violations
4. **Re-audit** - Run this skill again after changes
5. **Integrate into CI/CD** - Prevent regressions with automated checks

---

## Reference Materials

For detailed evaluation criteria, consult:
- [Scoring Rubric](.github/skills/hexagonal-architecture-audit/references/scoring-rubric.md)
- [Domain Independence](.github/skills/hexagonal-architecture-audit/references/domain-independence.md)
- [Ports Definition](.github/skills/hexagonal-architecture-audit/references/ports-definition.md)
- [Adapter Isolation](.github/skills/hexagonal-architecture-audit/references/adapter-isolation.md)
- [Dependency Inversion](.github/skills/hexagonal-architecture-audit/references/dependency-inversion.md)
- [Application Layer](.github/skills/hexagonal-architecture-audit/references/application-layer.md)
- [Testability](.github/skills/hexagonal-architecture-audit/references/testability.md)
- [Model Immutability](.github/skills/hexagonal-architecture-audit/references/model-immutability.md)

---

**Report Generated**: {TIMESTAMP}  
**Skill Version**: 1.0  
**Framework**: Hexagonal Architecture Audit  
**Keywords**: Hexagonal, Ports & Adapters, Domain-Driven Design, Architecture Validation
