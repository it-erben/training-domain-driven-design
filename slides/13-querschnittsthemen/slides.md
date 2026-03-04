---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 3"
footer: "© 2026 – Workshop S2090"
style: |
  section {
    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  }
  h1 {
    color: #2d6a4f;
  }
  h2 {
    color: #40916c;
  }
  code {
    background-color: #f0f0f0;
    border-radius: 4px;
    padding: 2px 6px;
  }
---

# Modul 13 – Querschnittsthemen

## Optimistic Locking, Auditing & Multi-Tenancy in Clean Architecture

**Geschätzte Dauer:** ca. 60 Minuten

### Lernziele

- Optimistic Locking auf Aggregate Roots anwenden — auch mit separatem Persistenzmodell
- JPA Auditing für automatische Zeitstempel konfigurieren
- Multi-Tenancy-Strategien bewerten
- Querschnittsthemen in die **richtige Schicht** der Clean Architecture einordnen
- Soft Delete als Alternative zu physischem Löschen kennen

---

## Überblick: Was sind Querschnittsthemen?

Querschnittsthemen betreffen **mehrere Schichten** — sie müssen sauber integriert werden, ohne die Domäne zu verschmutzen.

| Thema | Betroffene Schicht | Schlüssel-Annotation |
|-------|-------------------|---------------------|
| **Optimistic Locking** | Infrastructure (JPA Entity) | `@Version` |
| **Auditing** | Infrastructure (JPA Entity) | `@CreatedDate`, `@LastModifiedDate` |
| **Multi-Tenancy** | Infrastructure (Filter/Schema) | Hibernate `@Filter` |
| **Soft Delete** | Infrastructure (JPA Entity) | `@Where` / `@SQLRestriction` |
| **Exception Handling** | Adapter (Web) | `@RestControllerAdvice` (→ Modul 09) |

> **Kernregel:** Die Domain bleibt **frei** von diesen Concerns.
> Alle technischen Annotationen leben auf der **JPA-Entity** in `infrastructure`.

---

## Optimistic Locking – Warum?

- Aggregate Roots sind **Konsistenzgrenzen**
- Mehrere Benutzer können **gleichzeitig** dasselbe Aggregat bearbeiten
- Ohne Locking: **Lost Updates** — letzte Änderung gewinnt stillschweigend

### Szenario im Immobilien-CRM

```
Makler A                              Makler B
   │                                     │
   ├─ lädt Vorgang #42 (Version 1)       ├─ lädt Vorgang #42 (Version 1)
   │                                     │
   ├─ ändert Besichtigung                │
   ├─ speichert → Version 2 ✅           │
   │                                     ├─ ändert Angebot
   │                                     ├─ speichert → ❌ Konflikt!
   │                                     │   (erwartet V1, ist aber V2)
```

---

## @Version in Clean Architecture

### Wo lebt die Version?

```
┌── domain.model ──────────────────┐    ┌── infrastructure.persistence ──┐
│                                   │    │                                │
│ class Vermittlungsvorgang {       │    │ @Entity                        │
│   private final VorgangId id;     │    │ class VorgangJpaEntity {       │
│   private int version;            │    │   @Id UUID id;                 │
│   // Geschäftslogik               │    │   @Version Long version;       │
│ }                                 │    │   // JPA-Felder                │
└───────────────────────────────────┘    └────────────────────────────────┘
```

- Die Domain hat ein **einfaches `version`-Feld** (int) — ohne JPA-Annotation
- Die JPA-Entity hat `@Version` — JPA prüft automatisch beim UPDATE
- Der **Mapper** überträgt die Version in beide Richtungen

---

## @Version auf der JPA-Entity

```java
package de.immobiliencrm.vermittlung.infrastructure.persistence;

@Entity
@Table(name = "vermittlungsvorgang")
public class VorgangJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private VorgangStatus status;

    private UUID immobilieId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "vorgang_id")
    private List<BesichtigungJpaEntity> besichtigungen = new ArrayList<>();

    protected VorgangJpaEntity() {}
    // Getter, Setter
}
```

---

## Was passiert bei einem Konflikt?

```sql
-- JPA generiert automatisch:
UPDATE vermittlungsvorgang
SET status = ?, immobilie_id = ?, version = 2
WHERE id = ? AND version = 1;
-- Wenn 0 Rows affected → OptimisticLockException
```

### Exception-Kette

```
JPA: OptimisticLockException
  ↓
Spring Data: OptimisticLockingFailureException
  ↓
@RestControllerAdvice: HTTP 409 Conflict
```

```java
// Im @RestControllerAdvice (bereits aus Modul 09)
@ExceptionHandler(OptimisticLockingFailureException.class)
public ProblemDetail handleConflict(
        OptimisticLockingFailureException ex) {
    var problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT,
        "Die Daten wurden zwischenzeitlich geändert. "
        + "Bitte laden Sie die aktuelle Version neu.");
    problem.setTitle("Konflikt: gleichzeitige Änderung");
    return problem;
}
```

---

## Auditing – Wer hat wann was geändert?

- Für **Compliance** und Nachvollziehbarkeit im Immobilien-CRM
- Spring Data JPA bietet **deklaratives Auditing**
- Automatische Befüllung von Zeitstempeln und Benutzerinformationen

### Aktivierung

```java
package de.immobiliencrm.infrastructure.config;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(
            SecurityContextHolder.getContext().getAuthentication())
            .map(Authentication::getName);
    }
}
```

> Auditing-Konfiguration lebt in `infrastructure.config` — nicht in der Domain.

---

## Auditable Base Entity

```java
package de.immobiliencrm.infrastructure.persistence;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableJpaEntity {

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @CreatedBy
    @Column(updatable = false)
    private String createdBy;

    @LastModifiedBy
    private String updatedBy;

    // Getter
}
```

- `AuditableJpaEntity` lebt in **`infrastructure.persistence`** — nicht in der Domain!
- JPA-Entities erben davon, **nicht** die Domain-Klassen
- JPA befüllt die Felder automatisch bei `persist` und `merge`

---

## Auditing in der Praxis

```java
@Entity
@Table(name = "vermittlungsvorgang")
public class VorgangJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private VorgangStatus status;

    // ...
}
```

- `createdAt` / `createdBy` werden beim **Anlegen** gesetzt
- `updatedAt` / `updatedBy` werden bei **jeder Änderung** aktualisiert
- Die Domain-Klasse `Vermittlungsvorgang` **weiß davon nichts**
- Auditing ist ein **rein technisches Concern** der Infrastruktur

---

## Soft Delete – Alternative zum physischen Löschen

### Warum?

- Regulatorische Anforderungen (Aufbewahrungspflichten)
- "Papierkorb"-Funktion für Endbenutzer
- Audit-Trail bleibt erhalten

```java
@Entity
@Table(name = "vermittlungsvorgang")
@SQLRestriction("deleted = false")  // Hibernate 6.4+
public class VorgangJpaEntity extends AuditableJpaEntity {

    @Id private UUID id;
    @Version private Long version;

    private boolean deleted = false;
    private Instant deletedAt;

    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = Instant.now();
    }
}
```

- `@SQLRestriction` filtert gelöschte Einträge **automatisch** aus Queries
- Physisches Löschen wird durch `markAsDeleted()` ersetzt

---

## Multi-Tenancy – Überblick

Mandantenfähigkeit: Mehrere Maklerbüros auf einer Plattform

| Strategie | Isolation | Komplexität | Einsatz |
|-----------|-----------|-------------|---------|
| **Discriminator Column** | Niedrig | Niedrig | Kleine SaaS |
| **Separate Schema** | Mittel | Mittel | Mittlere SaaS |
| **Separate Database** | Hoch | Hoch | Enterprise / Compliance |

### Discriminator Column (einfachste Variante)

```java
@Entity
@FilterDef(name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter",
    condition = "tenant_id = :tenantId")
public class VorgangJpaEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
}
```

> Multi-Tenancy ist ein **reines Infrastruktur-Concern**.
> Die Domain kennt keine Mandanten.

---

## Einordnung: Querschnittsthemen in Clean Architecture

```
┌─────────────────────────────────────────────────────────────┐
│ adapter.web                                                  │
│   @RestControllerAdvice → ProblemDetail (Modul 09)          │
│   OptimisticLockingFailureException → 409 Conflict          │
├─────────────────────────────────────────────────────────────┤
│ application.service                                          │
│   @Transactional → Unit of Work                             │
│   Kein Wissen über Locking, Auditing, Tenancy               │
├─────────────────────────────────────────────────────────────┤
│ domain.model                                                 │
│   version-Feld (int) → aber KEINE @Version-Annotation       │
│   Kein Auditing, kein Soft Delete, kein Tenant               │
├─────────────────────────────────────────────────────────────┤
│ infrastructure.persistence                                   │
│   @Version, @CreatedDate, @LastModifiedDate                  │
│   @SQLRestriction, @Filter (Multi-Tenancy)                  │
│   AuditableJpaEntity (MappedSuperclass)                     │
└─────────────────────────────────────────────────────────────┘
```

> **Faustregel:** Wenn es eine JPA/Hibernate-Annotation braucht,
> gehört es in `infrastructure.persistence`.

---

## Zusammenfassung

| Thema | Wo? | Domain betroffen? |
|-------|-----|-------------------|
| **Optimistic Locking** | `@Version` auf JPA-Entity | Nur `version`-Feld (ohne Annotation) |
| **Auditing** | `AuditableJpaEntity` in infrastructure | Nein |
| **Soft Delete** | `@SQLRestriction` auf JPA-Entity | Nein |
| **Multi-Tenancy** | `@Filter` + Discriminator auf JPA-Entity | Nein |
| **Exception Mapping** | `@RestControllerAdvice` in adapter | Nein |

- Querschnittsthemen leben in der **richtigen Schicht** — nicht in der Domain
- Die Domain bleibt **rein und testbar**
- `@RestControllerAdvice` für Fehlerbehandlung → siehe Modul 09

---

## 🎯 Hands-on: Lab-11

### Aufgabe

1. `@Version` auf der JPA-Entity `VorgangJpaEntity` ergänzen
2. `version`-Feld im Domain-Model und Mapper hinzufügen
3. `AuditableJpaEntity` als MappedSuperclass erstellen (in `infrastructure`)
4. JPA Auditing aktivieren (`@EnableJpaAuditing`)
5. `@RestControllerAdvice` um `OptimisticLockingFailureException` → 409 erweitern
6. **Bonus:** Test schreiben, der `OptimisticLockException` provoziert

> **Dauer:** ca. 30 Minuten
> Details und Aufgabenstellung im **Lab-11**

---

## 💬 Diskussion

> Wie ordnet ihr Querschnittsthemen in eure Architektur ein?

- Wo lebt Auditing in euren Projekten — in der Domain oder Infrastruktur?
- Nutzt ihr **Optimistic** oder **Pessimistic** Locking?
- Welche Multi-Tenancy-Strategie passt zu eurem Kontext?
- Habt ihr Erfahrungen mit **Soft Delete** — Fluch oder Segen?
- Was passiert, wenn `@Version` auf dem Domain-Model statt der JPA-Entity liegt?
