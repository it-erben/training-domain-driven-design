# Lab 05: Clean Architecture Refactoring

## Lernziel

Code aus Lab-04 in die Clean-Architecture-Paketstruktur überführen.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 04 abgeschlossen
- Slides Modul 06 und 07

## Aufgabe

Refaktoriere den Code aus Lab-04 in folgende Paketstruktur:

```
de.immobiliencrm.vermittlung/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA Implementierung des Repository)
```

### Schritt 1: Paketstruktur anlegen

Erstelle die oben gezeigte Paketstruktur unter `de.immobiliencrm.vermittlung`.

### Schritt 2: Domain-Schicht befüllen

Übernimm die folgenden Klassen aus Lab-04 in die entsprechenden Pakete:

- `domain/model/`: `Adresse`, `Preisvorstellung`, `Provision`, `VermittlungsvorgangStatus`, `Besichtigung`, `Angebot`, `Vermittlungsvorgang`
- `domain/port/`: `VermittlungsvorgangRepository` (reines Java-Interface)
- `domain/event/`: `BesichtigungDurchgeführt`, `AngebotEingegangen`, `AngebotAngenommen`

**Wichtig:** KEINE Spring-Imports in der gesamten `domain`-Schicht! Die Domain-Schicht darf ausschließlich Standard-Java-Klassen verwenden.

### Schritt 3: JPA-Mapping in Infrastructure erstellen

Erstelle die folgenden Klassen im Package `infrastructure/persistence/`:

**JpaVermittlungsvorgang** - JPA `@Entity` mit Jakarta Persistence Annotations:

- Alle Felder des Domain-Modells als JPA-kompatible Felder
- `@Id` und `@GeneratedValue` für die ID
- `@ElementCollection` für `besichtigungen` und `angebote`
- Methoden `toModel()` und `static fromModel()` für die Konvertierung zwischen Domain-Modell und JPA-Entity

**JpaBesichtigung** - `@Embeddable` mit JPA-Feldern

**JpaAngebot** - `@Embeddable` mit JPA-Feldern

**JpaVermittlungsvorgangRepository** - Interface, das `JpaRepository<JpaVermittlungsvorgang, UUID>` erweitert

**VermittlungsvorgangRepositoryAdapter** - `@Component`, implementiert das Domain-Interface `VermittlungsvorgangRepository`:

- Injiziert `JpaVermittlungsvorgangRepository`
- Mappt zwischen Domain-Objekten und JPA-Entities

### Schritt 4: Application Service erstellen

Erstelle `VermittlungsvorgangApplicationService` im Package `application/service/`:

- `@Service`, `@Transactional`
- Injiziert `VermittlungsvorgangRepository` (Domain-Port-Interface) per Constructor Injection
- Methoden:
  - `erstellen(UUID immobilieId, Adresse adresse, Preisvorstellung preis, Provision provision)` - erstellt und speichert einen neuen Vermittlungsvorgang
  - `findById(UUID id)` - gibt `Optional<Vermittlungsvorgang>` zurück

## Verifikation

1. Projekt kompiliert:

```bash
cd solution
mvn compile
```

2. Keine Spring-Imports in `domain/`:

```bash
grep -r "org.springframework" src/main/java/de/immobiliencrm/vermittlung/domain/
```

Dieser Befehl darf keine Treffer liefern.

3. Application Service kann über Constructor Injection den Repository-Port injiziert bekommen.

4. Tests laufen durch:

```bash
mvn test
```

## Bonus

Prüfe mit einem einfachen Grep/Find, dass keine `org.springframework`-Imports in `domain/` existieren:

```bash
find src/main/java/de/immobiliencrm/vermittlung/domain -name "*.java" \
  -exec grep -l "org.springframework" {} \;
```

Das Ergebnis muss leer sein - kein einziger Treffer.

## Tipps

- Die Domain-Schicht kennt weder Spring noch JPA. Sie enthält reines Java.
- Die Infrastructure-Schicht implementiert die Ports der Domain-Schicht und kümmert sich um die technische Persistenz.
- Die Application-Schicht orchestriert die Use Cases und nutzt die Ports der Domain-Schicht.
- Der Adapter in der Infrastructure-Schicht übernimmt das Mapping zwischen Domain-Modell und JPA-Entity. Dadurch bleibt das Domain-Modell frei von technischen Annotationen.
