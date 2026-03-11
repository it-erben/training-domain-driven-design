---
marp: true
theme: default
paginate: true
header: "DDD & Clean Architecture mit Spring Boot 4"
footer: "CC BY-NC-SA 4.0, Alexander Erben"
---

# Modul 14 - Querschnittsthemen

## Optimistic Locking, Auditing & Multi-Tenancy in Clean Architecture

Geschätzte Dauer: ca. 60 Minuten

---

## Lernziele

- Optimistic Locking auf Aggregate Roots anwenden - auch mit separatem Persistenzmodell
- JPA Auditing für automatische Zeitstempel konfigurieren
- Multi-Tenancy-Strategien bewerten
- Querschnittsthemen in die richtige Schicht der Clean Architecture einordnen
- Soft Delete als Alternative zu physischem Löschen kennen

---

## Überblick: Was sind Querschnittsthemen?

<style scoped>
section { font-size: 22px; }
table { font-size: 20px; }
</style>

Querschnittsthemen betreffen mehrere Schichten - sie müssen sauber integriert werden, ohne die Domäne zu verschmutzen.

| Thema | Betroffene Schicht | Schlüssel-Annotation |
|-------|-------------------|---------------------|
| Optimistic Locking | Infrastructure (JPA Entity) | `@Version` |
| Auditing | Infrastructure (JPA Entity) | `@CreatedDate`, `@LastModifiedDate` |
| Multi-Tenancy | Infrastructure (Filter/Schema) | Hibernate `@Filter` |
| Soft Delete | Infrastructure (JPA Entity) | `@Where` / `@SQLRestriction` |
| Exception Handling | Adapter (Web) | `@RestControllerAdvice` (→ Modul 10) |

> Kernregel: Die Domain bleibt frei von diesen Concerns.
> Alle technischen Annotationen leben auf der JPA-Entity in `infrastructure`.

---

## Optimistic Locking - Warum?

> Anna und Beate öffnen gleichzeitig denselben Förderantrag.
> Anna speichert zuerst. Beates Änderung überschreibt Annas Arbeit — **stillschweigend**.
> Das System meldet keinen Fehler. Die Änderung ist einfach weg.
> Das nennt sich **Lost Update** — und es ist ein fachliches Problem, kein technisches.

- Aggregate Roots sind Konsistenzgrenzen
- Mehrere Benutzer können gleichzeitig dasselbe Aggregat bearbeiten
- Ohne Locking: Lost Updates — letzte Änderung gewinnt stillschweigend
- **Fachliche Frage:** Muss das System den Konflikt erkennen? Wem gehört die Entscheidung?

---

![bg center h:500](images/optimistic-locking-szenario.drawio.svg)

---

![h:350](images/version-clean-architecture.drawio.svg)

- Die Domain hat ein einfaches `version`-Feld (int) - ohne JPA-Annotation
- Die JPA-Entity hat `@Version` - JPA prüft automatisch beim UPDATE
- Der Mapper überträgt die Version in beide Richtungen

---

## @Version auf der JPA-Entity

<style scoped>
pre { font-size: 16px; }
</style>

```java
package de.foerderung.antragstellung.infrastructure.persistence;

@Entity
@Table(name = "antragsmappe")
public class AntragsMappeJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private AntragStatus status;

    private String registrierungsNummer;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "antragsmappe_id")
    private List<FlurstueckJpaEntity> flurstuecke = new ArrayList<>();

    protected AntragsMappeJpaEntity() {}
    // Getter, Setter
}
```

---

## Was passiert bei einem Konflikt?

```sql
-- JPA generates automatically:
UPDATE antragsmappe
SET status = ?, registrierungs_nummer = ?, version = 2
WHERE id = ? AND version = 1;
-- If 0 rows affected → OptimisticLockException
```

### Exception-Kette

```
JPA: OptimisticLockException
  ↓
Spring Data: OptimisticLockingFailureException
  ↓
@RestControllerAdvice: HTTP 409 Conflict
```

---

## Konflikt-Behandlung im @RestControllerAdvice

```java
// In @RestControllerAdvice (from Module 10)
@ExceptionHandler(OptimisticLockingFailureException.class)
public ProblemDetail handleConflict(
        OptimisticLockingFailureException ex) {
    var problem = ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT,
        "Die Daten wurden zwischenzeitlich verändert. "
        + "Bitte die aktuelle Version neu laden.");
    problem.setTitle("Konflikt: gleichzeitige Änderung");
    return problem;
}
```

---
<style scoped>section { font-size: 1.6em; }</style>

## Auditing - Wer hat wann was geändert?

- Für Compliance und Nachvollziehbarkeit in der Förderantragsverwaltung
- Spring Data JPA bietet deklaratives Auditing
- Automatische Befüllung von Zeitstempeln und Benutzerinformationen

### Aktivierung

```java
package de.foerderung.infrastructure.config;

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

> Auditing-Konfiguration lebt in `infrastructure.config` - nicht in der Domain.

---

## Auditable Base Entity

<style scoped>
section { font-size: 19px; }
pre { font-size: 15px; }
</style>

```java
package de.foerderung.infrastructure.persistence;

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

    // Getters
}
```

- `AuditableJpaEntity` lebt in `infrastructure.persistence` - nicht in der Domain!
- JPA-Entities erben davon, nicht die Domain-Klassen
- JPA befüllt die Felder automatisch bei `persist` und `merge`

---

## Auditing in der Praxis - Code

```java
@Entity
@Table(name = "antragsmappe")
public class AntragsMappeJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    private AntragStatus status;

    // ...
}
```

---

## Auditing in der Praxis - Erklärung

- `createdAt` / `createdBy` werden beim Anlegen gesetzt
- `updatedAt` / `updatedBy` werden bei jeder Änderung aktualisiert
- Die Domain-Klasse `AntragsMappe` weiß davon nichts
- Auditing ist ein rein technisches Concern der Infrastruktur

---

## Soft Delete - Warum?

- Regulatorische Anforderungen (Aufbewahrungspflichten)
- "Papierkorb"-Funktion für Endbenutzer
- Audit-Trail bleibt erhalten

---

## Soft Delete - Implementierung (Hibernate 7)

<style scoped>
section { font-size: 22px; }
pre { font-size: 18px; }
</style>

```java
@Entity
@Table(name = "antragsmappe")
@SoftDelete(columnName = "deleted")  // Hibernate 7 — nativ, kein Custom-Code
public class AntragsMappeJpaEntity extends AuditableJpaEntity {

    @Id private UUID id;
    @Version private Long version;

    @Enumerated(EnumType.STRING)
    private AntragStatus status;
    // 'deleted'-Spalte wird von Hibernate automatisch verwaltet!
}
```

```java
// Repository-Adapter: delete() ruft jpaRepo.delete() auf →
// Hibernate setzt 'deleted = true', kein physisches DELETE
@Override
public void delete(AntragsMappe mappe) {
    jpaRepo.findById(mappe.getId().value())
        .ifPresent(jpaRepo::delete);  // → UPDATE ... SET deleted = true
}
```

- `@SoftDelete` fügt die `deleted`-Spalte automatisch hinzu und filtert sie in allen Queries
- Kein manuelles `markAsDeleted()` nötig — Hibernate 7 übernimmt alles
- Domain bleibt vollständig frei von diesem Infrastruktur-Concern

---

## Multi-Tenancy - Überblick

Mandantenfähigkeit: Mehrere Förderagenturen auf einer Plattform

| Strategie | Isolation | Komplexität | Einsatz |
|-----------|-----------|-------------|---------|
| Discriminator Column | Niedrig | Niedrig | Kleine SaaS |
| Separate Schema | Mittel | Mittel | Mittlere SaaS |
| Separate Database | Hoch | Hoch | Enterprise / Compliance |

---

## Multi-Tenancy - Discriminator Column

### Einfachste Variante

```java
@Entity
@FilterDef(name = "tenantFilter",
    parameters = @ParamDef(name = "tenantId", type = String.class))
@Filter(name = "tenantFilter",
    condition = "tenant_id = :tenantId")
public class AntragsMappeJpaEntity {

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;
}
```

> Multi-Tenancy ist ein reines Infrastruktur-Concern.
> Die Domain kennt keine Mandanten.

---

![bg center h:500](images/querschnittsthemen-schichten.drawio.svg)

---

## Zusammenfassung

<style scoped>
section { font-size: 22px; }
table { font-size: 20px; }
</style>

| Thema | Wo? | Domain betroffen? |
|-------|-----|-------------------|
| Optimistic Locking | `@Version` auf JPA-Entity | Nur `version`-Feld (ohne Annotation) |
| Auditing | `AuditableJpaEntity` in infrastructure | Nein |
| Soft Delete | `@SoftDelete` auf JPA-Entity (Hibernate 7) | Nein |
| Multi-Tenancy | `@Filter` + Discriminator auf JPA-Entity | Nein |
| Exception Mapping | `@RestControllerAdvice` in adapter | Nein |

- Querschnittsthemen leben in der richtigen Schicht - nicht in der Domain
- Die Domain bleibt rein und testbar
- `@RestControllerAdvice` für Fehlerbehandlung → siehe Modul 10

---

## Reflexion: Prüft euer Verständnis

1. Wo lebt `@Version` — im Domain-Model oder im JPA-Entity? Warum?
2. Warum gehört Auditing (`createdAt`, `modifiedAt`) in die Infrastruktur-Schicht?
3. Was passiert bei einem Optimistic-Locking-Konflikt — und wie kommuniziert ihr das dem Nutzer?

> Im Lab ergänzt ihr Optimistic Locking, Auditing und Soft Delete in der Förderantragsverwaltung.

---

## Hands-on: Lab 11

### Aufgabe

1. `@Version` auf der JPA-Entity `AntragsMappeJpaEntity` ergänzen
2. `version`-Feld im Domain-Model und Mapper hinzufügen
3. `AuditableJpaEntity` als MappedSuperclass erstellen (in `infrastructure`)
4. JPA Auditing aktivieren (`@EnableJpaAuditing`)
5. `@RestControllerAdvice` um `OptimisticLockingFailureException` → 409 erweitern
6. Bonus: Test schreiben, der `OptimisticLockException` provoziert

> Dauer: ca. 30 Minuten

---

## Diskussion

> Wie ordnet ihr Querschnittsthemen in eure Architektur ein?

**Optimistic Locking und Versionierung:**

- Ein typisches Problem in der Praxis: Optimistic Lock Exception wirft einen rohen JPA-Fehler.
  Wie würdet ihr eine fachliche `KonkurrenteAenderungException` modellieren?
- Wo passiert in euren Systemen Lost Update, weil kein Locking vorhanden ist?
- Was ist der Unterschied zwischen Optimistic und Pessimistic Locking — wann welches?
- **Domain Object Versioning (Fowler Temporal Pattern):** Wie unterscheidet sich das Speichern des *aktuellen Zustands* von der *Zustandshistorie*?
  Das Temporal Pattern trennt `Entity` (Kontinuität der Identität) von `Snapshot` (Zustand zu einem Zeitpunkt).
  In welchen fachlichen Szenarien ist lückenlose Zustandshistorie ein regulatorisches Muss?

**Auditing und Multi-Tenancy:**

- Wo lebt Auditing in euren Projekten — in der Domain oder Infrastruktur?
- Welche Multi-Tenancy-Strategie passt zu eurem Kontext (Discriminator / Schema / DB)?
- Ein Mandant darf keine Daten eines anderen Mandanten sehen oder ändern.
  Wie sichert ihr das auf Aggregate-Ebene ab?

**Soft Delete:**

- Habt ihr Erfahrungen mit Soft Delete — Fluch oder Segen?
- Was passiert, wenn `@Version` auf dem Domain-Model statt der JPA-Entity liegt?

> Evans, „Domain-Driven Design" (2003), S. 125: Aggregate-Invarianten als erste Verteidigungslinie
> Khononov, „Einführung in Domain-Driven Design" (2022), Kapitel 6: Bausteine des Domain Model
