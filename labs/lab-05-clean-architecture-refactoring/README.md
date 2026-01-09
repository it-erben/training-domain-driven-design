# Lab 05: Clean Architecture Refactoring

Refactore den Code aus Lab 04 in die folgende Paketstruktur:

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

Außerdem überführst du den Code aus Lab 01 bezüglich Immobilienverwaltung in
die gleiche Struktur.

## Schritt 1: Paketstruktur anlegen

Lege die oben gezeigte Paketstruktur unter `de.realestate.brokerage` an.

## Schritt 2: Domain-Schicht befüllen

Verschiebe die folgenden Klassen aus Lab 04 in die entsprechenden Packages:

- `domain/model/`: `AskingPrice`, `Commission`, `ProcessStatus`,
  `Viewing`, `Offer`, `BrokerageProcess`
- `domain/port/`: `BrokerageProcessRepository` (reines Java-Interface)
- `domain/event/`: `ViewingCompleted`, `OfferReceived`, `OfferAccepted`

Wichtig: KEINE Spring-Imports in der gesamten `domain`-Schicht! Die
Domain-Schicht darf nur Standard-Java-Klassen verwenden.

## Schritt 3: JPA-Mapping in der Infrastruktur erstellen

Erstelle die folgenden Klassen im Package `infrastructure/persistence/`:

JpaBrokerageProcess - JPA-`@Entity` mit Jakarta-Persistence-Annotationen:

- Alle Felder des Domain-Modells als JPA-kompatible Felder
- `@Id` für die ID
- `@ElementCollection` für `viewings` und `offers`
- Methoden `toModel()` und `static fromModel()` zur Konvertierung zwischen
  Domain-Modell und JPA-Entity

JpaViewing - `@Embeddable` mit JPA-Feldern

JpaOffer - `@Embeddable` mit JPA-Feldern

JpaBrokerageProcessRepository - Interface, das
`JpaRepository<JpaBrokerageProcess, UUID>` erweitert

BrokerageProcessRepositoryAdapter - `@Component`, implementiert das
Domain-Interface `BrokerageProcessRepository`:

- Injiziert `JpaBrokerageProcessRepository`
- Mappt zwischen Domain-Objekten und JPA-Entities

## Schritt 4: Application Service erstellen

Erstelle `BrokerageProcessApplicationService` im Package `application/service/`:

- `@Service`, `@Transactional`
- Hat ein `BrokerageProcessRepository` (Domain-Port-Interface) als Feld
- Methoden:
  - `create(UUID propertyId, AskingPrice askingPrice, Commission commission)` -
    erstellt und persistiert einen neuen BrokerageProcess
  - `findById(UUID id)` - gibt `Optional<BrokerageProcess>` zurück

## (OPTIONAL) Schritt 5: Property-Domäne neu strukturieren

Gehe nun genauso mit dem Code aus Lab 01 vor, der noch in dem Paket
`de.realestate.property` liegt.

```
de.realestate.property/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA-Implementierung des Repositories)
```

Momentan befindet sich im `property`-Paket nur Code, der Infrastruktur-
Abhängigkeiten hat. Lege neue Klassen an, um die obige Aufteilung zu erhalten.

## Ein paar Hinweise

- Die Domain-Schicht kennt weder Spring noch JPA. Sie enthält reines Java.
- Die Infrastruktur-Schicht implementiert die Ports der Domain-Schicht und
  kümmert sich um die technische Persistenz.
- Die Application-Schicht orchestriert Use Cases und nutzt die Ports der
  Domain-Schicht.
- Der Adapter in der Infrastruktur-Schicht übernimmt das Mapping zwischen
  Domain-Modell und JPA-Entity. So bleibt das Domain-Modell frei von technischen
  Annotationen.
