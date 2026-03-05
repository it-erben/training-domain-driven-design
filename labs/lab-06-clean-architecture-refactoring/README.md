# Lab 06: Clean Architecture Refactoring

## Lernziel

Den Code aus Lab 05 in eine Clean-Architecture-Paketstruktur überführen.

## Dauer

60 Minuten

## Voraussetzungen

- Lab 05 abgeschlossen
- Slides Modul 07 und 08

## Aufgabe

Refaktoriere den Code aus Lab 05 in die folgende Paketstruktur:

```
de.realestate.brokerage/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA-Implementierung des Repositories)
```

### Schritt 1: Paketstruktur anlegen

Lege die oben gezeigte Paketstruktur unter `de.realestate.brokerage` an.

### Schritt 2: Domain-Schicht befüllen

Verschiebe die folgenden Klassen aus Lab 05 in die entsprechenden Packages:

- `domain/model/`: `Address`, `AskingPrice`, `Commission`, `ProcessStatus`, `Viewing`, `Offer`, `BrokerageProcess`
- `domain/port/`: `BrokerageProcessRepository` (reines Java-Interface)
- `domain/event/`: `ViewingCompleted`, `OfferReceived`, `OfferAccepted`

**Wichtig:** KEINE Spring-Imports in der gesamten `domain`-Schicht! Die Domain-Schicht darf nur Standard-Java-Klassen verwenden.

### Schritt 3: JPA-Mapping in der Infrastruktur erstellen

Erstelle die folgenden Klassen im Package `infrastructure/persistence/`:

**JpaBrokerageProcess** – JPA-`@Entity` mit Jakarta-Persistence-Annotationen:

- Alle Felder des Domain-Modells als JPA-kompatible Felder
- `@Id` und `@GeneratedValue` für die ID
- `@ElementCollection` für `viewings` und `offers`
- Methoden `toModel()` und `static fromModel()` zur Konvertierung zwischen Domain-Modell und JPA-Entity

**JpaViewing** – `@Embeddable` mit JPA-Feldern

**JpaOffer** – `@Embeddable` mit JPA-Feldern

**JpaBrokerageProcessRepository** – Interface, das `JpaRepository<JpaBrokerageProcess, UUID>` erweitert

**BrokerageProcessRepositoryAdapter** – `@Component`, implementiert das Domain-Interface `BrokerageProcessRepository`:

- Injiziert `JpaBrokerageProcessRepository`
- Mappt zwischen Domain-Objekten und JPA-Entities

### Schritt 4: Application Service erstellen

Erstelle `BrokerageProcessApplicationService` im Package `application/service/`:

- `@Service`, `@Transactional`
- Injiziert `BrokerageProcessRepository` (Domain-Port-Interface) per Constructor Injection
- Methoden:
  - `create(UUID propertyId, Address address, AskingPrice askingPrice, Commission commission)` – erstellt und persistiert einen neuen BrokerageProcess
  - `findById(UUID id)` – gibt `Optional<BrokerageProcess>` zurück

## Verifikation

1. Projekt kompiliert:

```bash
cd ../../solutions/lab-06-clean-architecture-refactoring
mvn compile
```

2. Keine Spring-Imports in `domain/`:

```bash
grep -r "org.springframework" src/main/java/de/realestate/brokerage/domain/
```

Dieser Befehl darf keine Ergebnisse liefern.

3. Der Application Service kann das Repository-Port per Constructor Injection empfangen.

4. Tests sind grün:

```bash
mvn test
```

## Bonus

Überprüfe mit einem einfachen grep/find, dass keine `org.springframework`-Imports in `domain/` existieren:

```bash
find src/main/java/de/realestate/brokerage/domain -name "*.java" \
  -exec grep -l "org.springframework" {} \;
```

Das Ergebnis muss leer sein – keine Treffer.

## Tipps

- Die Domain-Schicht kennt weder Spring noch JPA. Sie enthält reines Java.
- Die Infrastruktur-Schicht implementiert die Ports der Domain-Schicht und kümmert sich um die technische Persistenz.
- Die Application-Schicht orchestriert Use Cases und nutzt die Ports der Domain-Schicht.
- Der Adapter in der Infrastruktur-Schicht übernimmt das Mapping zwischen Domain-Modell und JPA-Entity. So bleibt das Domain-Modell frei von technischen Annotationen.
