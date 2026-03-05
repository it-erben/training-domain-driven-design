# Lab 08: ArchUnit - Architecture Rules as Tests

## Learning Objective

Automatically verify architecture rules using ArchUnit.

## Duration

45 minutes

## Prerequisites

- Lab 07 completed
- Slides Module 10

## Task

Add ArchUnit tests to the project that ensure Clean Architecture rules are being followed.

### Step 1: Add ArchUnit Dependency

Add the ArchUnit dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <version>1.3.0</version>
    <scope>test</scope>
</dependency>
```

### Step 2: Create Test Class

Create the test class `ArchitectureTest` in the package `de.realestate.architecture` under `src/test/java`:

```java
@AnalyzeClasses(packages = "de.realestate")
class ArchitectureTest {
    // Define rules here
}
```

### Step 3: Rule 1 - Domain must not depend on Infrastructure or Adapter

The domain layer must have no dependencies on the infrastructure or adapter layers:

```java
@ArchTest
static final ArchRule domain_should_not_depend_on_infrastructure_or_adapter =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure..", "..adapter..");
```

### Step 4: Rule 2 - No Spring Framework Classes in Domain

The domain layer must not use any Spring Framework classes:

```java
@ArchTest
static final ArchRule domain_has_no_spring_imports =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .resideInAPackage("org.springframework..");
```

### Step 5: Rule 3 - Adapter.Web must not access Domain.Model directly

The web adapter must not access `domain.model` directly, only through the application layer:

```java
@ArchTest
static final ArchRule web_adapter_should_not_access_domain_model_directly =
    noClasses()
        .that().resideInAPackage("..adapter.web..")
        .should().dependOnClassesThat()
        .resideInAPackage("..domain.model..");
```

### Step 6: Rule 4 - Only Adapter.Web may use @RestController

Only classes in the `adapter.web` package may use the `@RestController` annotation:

```java
@ArchTest
static final ArchRule only_web_adapter_should_use_rest_controller =
    classes()
        .that().areAnnotatedWith(RestController.class)
        .should().resideInAPackage("..adapter.web..");
```

### Bonus: Rule for @Transactional

Ensure that `@Transactional` is only used in `application.service`:

```java
@ArchTest
static final ArchRule transactional_only_in_application_service =
    noClasses()
        .that().resideOutsideOfPackage("..application.service..")
        .should().beAnnotatedWith(Transactional.class);
```

## Verification

Run the tests:

```bash
cd solution
mvn test
```

All ArchUnit tests must pass.

## Tips

- ArchUnit analyzes compiled bytecode, so the project must be compiled first.
- Use `@AnalyzeClasses(packages = "de.realestate")` to analyze all classes in the project.
- ArchUnit rules can also be defined as fields with `@ArchTest` -- this is cleaner than individual test methods.
- If a rule fails, ArchUnit shows exactly which class violates which rule.
