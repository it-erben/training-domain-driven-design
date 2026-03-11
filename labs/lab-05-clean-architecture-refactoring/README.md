# Lab 05: Clean Architecture Refactoring

Refactore den Code aus Lab 04 in die folgende Paketstruktur:

```
de.foerderung.antragstellung/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA-Implementierung des Repositories)
```

Außerdem überführst du den Code aus Lab 01 bezüglich Betriebsinhaber in
die gleiche Struktur.

## Schritt 1: Paketstruktur anlegen

Lege die oben gezeigte Paketstruktur unter `de.foerderung.antragstellung` an.

## Schritt 2: Domain-Schicht befüllen

Verschiebe die folgenden Klassen aus Lab 04 in die entsprechenden Packages:

- `domain/model/`: `Foerderbetrag`, `Foerderquote`, `FlurstueckNummer`,
  `RegistrierungsNummer`, `AntragStatus`, `Flurstueck`, `Nachweis`,
  `AntragsMappe`
- `domain/port/`: `AntragsMappeRepository` (reines Java-Interface)
- `domain/event/`: `AntragEvent`, `FlurstueckHinzugefuegt`,
  `NachweisEingereicht`, `NachweisAkzeptiert`, `AntragsmappeEingereicht`

Wichtig: KEINE Spring-Imports in der gesamten `domain`-Schicht! Die
Domain-Schicht darf nur Standard-Java-Klassen verwenden.

## Schritt 3: JPA-Mapping in der Infrastruktur erstellen

Erstelle die folgenden Klassen im Package `infrastructure/persistence/`:

JpaAntragsMappe - JPA-`@Entity` mit Jakarta-Persistence-Annotationen:

- Alle Felder des Domain-Modells als JPA-kompatible Felder
- `@Id` für die ID
- `@ElementCollection` für `flurstuecke` und `nachweise`
- Methoden `toModel()` und `static fromModel()` zur Konvertierung zwischen
  Domain-Modell und JPA-Entity

JpaFlurstueck - `@Embeddable` mit JPA-Feldern

JpaNachweis - `@Embeddable` mit JPA-Feldern

JpaAntragsMappeRepository - Interface, das
`JpaRepository<JpaAntragsMappe, UUID>` erweitert

AntragsMappeRepositoryAdapter - `@Component`, implementiert das
Domain-Interface `AntragsMappeRepository`:

- Injiziert `JpaAntragsMappeRepository`
- Mappt zwischen Domain-Objekten und JPA-Entities

## Schritt 4: Application Service erstellen

Erstelle `AntragsMappeApplicationService` im Package `application/service/`:

- `@Service`, `@Transactional`
- Hat ein `AntragsMappeRepository` (Domain-Port-Interface) als Feld
- Methoden:
  - `erstellen(RegistrierungsNummer, Foerderbetrag, Foerderquote)` -
    erstellt und persistiert eine neue AntragsMappe
  - `findById(UUID id)` - gibt `Optional<AntragsMappe>` zurück

## (OPTIONAL) Schritt 5: Betriebsinhaber-Domäne neu strukturieren

Gehe nun genauso mit dem Code aus Lab 01 vor, der noch in dem Paket
`de.foerderung.betriebsinhaber` liegt.

```
de.foerderung.betriebsinhaber/
├── domain/
│   ├── model/        (Aggregate Root, Entities, Value Objects, Enum)
│   ├── port/         (Repository Interface = Outbound Port)
│   └── event/        (Domain Events)
├── application/
│   └── service/      (Application Services)
└── infrastructure/
    └── persistence/  (JPA-Implementierung des Repositories)
```

Momentan befindet sich im `betriebsinhaber`-Paket nur Code, der Infrastruktur-
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
