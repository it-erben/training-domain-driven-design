# Lab 06: Use-Case-Implementierung

Implementiere zwei Use Cases als Application Services im Brokerage-Kontext:

1. Besichtigung anlegen (`CreateViewingUseCase`)
2. Besichtigung abschließen (`CompleteViewingUseCase`)

Beide Use Cases folgen dem gleichen Grundmuster: Aggregate laden, Domain-Methode
aufrufen, Aggregate speichern. Die Geschäftslogik bleibt dabei vollständig im
Domain-Modell.

## Schritt 1: Command-Objekte erstellen

Erstelle im Package `de.realestate.brokerage.application.command` zwei
Command-Records:

CreateViewingCommand mit den Feldern:

- `UUID processId` - Referenz auf den Vermittlungsprozess
- `String prospectName` - Name des Interessenten
- `LocalDateTime appointmentDate` - Besichtigungstermin

CompleteViewingCommand mit den Feldern:

- `UUID processId` - Referenz auf den Vermittlungsprozess
- `UUID viewingId` - Referenz auf die abzuschließende Besichtigung

## Schritt 2: Ergebnisobjekt erstellen

Erstelle `CreateViewingResult` als Record im selben Package mit den Feldern
`viewingId` und `processId` (beides `UUID`).

Hinweis: `CompleteViewingUseCase` gibt kein Ergebnis zurück (`void`), da die
Zustandsveränderung keine neue Ressource erzeugt.

## Schritt 3: Exception erstellen

Erstelle `ProcessNotFoundException` als `RuntimeException` im Package
`de.realestate.brokerage.domain.model`. Der Konstruktor nimmt eine `UUID`
entgegen und erzeugt eine aussagekräftige Fehlermeldung.

Hinweis: Die Exception liegt im Domain-Package, da sie ein Domänenkonzept
darstellt ("Es gibt keinen Vermittlungsprozess mit dieser ID").

## Schritt 4: CreateViewingUseCase implementieren

Erstelle den Application Service `CreateViewingUseCase` als `@Service` mit
`@Transactional` im Package `de.realestate.brokerage.application.service`.

Ablauf der `create(CreateViewingCommand)`-Methode:

1. Lade den `BrokerageProcess` anhand der `processId` aus dem
   `BrokerageProcessRepository`
2. Falls nicht gefunden: wirf `ProcessNotFoundException`
3. Rufe `addViewing(prospectName, appointmentDate)` auf dem Aggregate Root auf -
   die Methode gibt die neue Viewing-ID zurück
4. Speichere den aktualisierten `BrokerageProcess` über das Repository
5. Gib ein `CreateViewingResult` mit der neuen Viewing-ID und der Process-ID
   zurück

## Schritt 5: CompleteViewingUseCase implementieren

Erstelle den Application Service `CompleteViewingUseCase` als `@Service` mit
`@Transactional` im selben Package.

Ablauf der `complete(CompleteViewingCommand)`-Methode:

1. Lade den `BrokerageProcess` anhand der `processId` aus dem Repository
2. Falls nicht gefunden: wirf `ProcessNotFoundException`
3. Rufe `completeViewing(viewingId)` auf dem Aggregate Root auf
4. Speichere den aktualisierten `BrokerageProcess`

Beachte: Die Domain-Methode `completeViewing()` wirft selbst eine
`IllegalArgumentException`, wenn die Viewing-ID nicht existiert. Der Use Case
muss das nicht separat behandeln - das Domain-Modell schützt seine eigenen
Invarianten.

## Tests

Schreibe Unit-Tests für beide Use Cases mit einem gemockten Repository (
`@ExtendWith(MockitoExtension.class)`).

CreateViewingUseCaseTest:

1. *Happy Path:* BrokerageProcess existiert → Viewing wird erstellt → Ergebnis
   enthält korrekte IDs → `save()` wird aufgerufen
2. *Prozess nicht gefunden:* `ProcessNotFoundException` wird geworfen → `save()`
   wird nicht aufgerufen

CompleteViewingUseCaseTest:

3. *Happy Path:* Viewing wird als abgeschlossen markiert → Domain Event
   `ViewingCompleted` ist in `getDomainEvents()` vorhanden → `save()` wird
   aufgerufen
4. *Prozess nicht gefunden:* `ProcessNotFoundException` wird geworfen → `save()`
   wird nicht aufgerufen
5. *Viewing nicht gefunden:* `IllegalArgumentException` wird aus der Domain
   geworfen → `save()` wird nicht aufgerufen

## Hinweise

- Die Use Cases sind bewusst schlank - die Geschäftslogik liegt im
  Domain-Modell.
- Child Entities wie `Viewing` oder `Offer` werden nur über den Aggregate Root
  verändert, nicht direkt von außen.
- Commands und Results sind unveränderlich (Records) und gehören zur
  Application-Schicht.
- `@Transactional` stellt sicher, dass bei einer Exception ein Rollback erfolgt.
- Das Repository-Interface kommt aus der Domain-Schicht (Port) - der Use Case
  hängt nur von der Abstraktion ab, nicht von der konkreten Implementierung.
- Beachte den Unterschied: `CreateViewingUseCase` gibt ein Result zurück (neue
  Ressource erzeugt), `CompleteViewingUseCase` gibt `void` zurück (
  Statusänderung an bestehender Ressource).
