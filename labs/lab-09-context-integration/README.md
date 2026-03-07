# Lab 09: Context-Integration – Bounded Contexts verbinden

In unserem Immobilien-CRM gibt es bisher nur den Brokerage-Bounded Context, der
sich um die Vermarktung von Immobilien kümmert (Besichtigungen, Angebote etc.).
Aber wie entsteht eigentlich ein Vermarktungsprozess?

In der Fachdomäne beginnt alles mit einem **Maklervertrag**:
Ein Eigentümer beauftragt das Unternehmen mit der Vermarktung seiner Immobilie.
Erst wenn dieser Vertrag abgeschlossen ist, soll automatisch ein neuer
`BrokerageProcess` im Brokerage-BC angelegt werden. Diesen vorgelagerten Schritt
bilden wir in einem zweiten Bounded Context ab – der **Akquise** (Acquisition).
Der Acquisition-BC verwaltet die Maklerverträge
(`BrokerageContract`) und veröffentlicht bei Vertragsabschluss ein
Integrations-Event, auf das der Brokerage-BC reagiert.

## Schritt 1: Minimalen Acquisition-Bounded Context erstellen

Erstelle die Entity `BrokerageContract` im Package
`de.realestate.acquisition.domain.model`:

- Felder: `id` (UUID), `ownerId` (UUID), `propertyId` (UUID), `askingPrice` (
  BigDecimal), `currency` (String), `commissionPercentage` (BigDecimal),
  `closedAt` (LocalDateTime)
- Domain-Events-Liste (wie bei `BrokerageProcess`)
- Methode: `close()` setzt `closedAt` und registriert ein `ContractSigned`-Event

```java
public class BrokerageContract {

    private final UUID id;
    private final UUID ownerId;
    private final UUID propertyId;
    private final BigDecimal askingPrice;
    private final String currency;
    private final BigDecimal commissionPercentage;
    private LocalDateTime closedAt;
    private final transient List<Object> domainEvents = new ArrayList<>();

    // Private Constructor, factory methods create() und reconstruct()

    public void close() {
        // 1. closedAt setzen
        // 2. ContractSigned-Event in domainEvents registrieren
    }

    // getDomainEvents(), clearDomainEvents()
}
```

## Schritt 2: Integrations-Event erstellen

Erstelle das Integrations-Event `ContractSigned` als Record im Package
`de.realestate.acquisition.domain.event`. Das Event muss alle Informationen
enthalten, die der Brokerage-BC braucht, um einen BrokerageProcess anzulegen:

```java
public record ContractSigned(
        UUID contractId,
        UUID propertyId,
        LocalDateTime closedAt,
        BigDecimal askingPrice,
        String currency,
        BigDecimal commissionPercentage
) {
}
```

## Schritt 3: Application Service im Acquisition-BC

Erstelle den Service `CloseContractUseCase` im Package
`de.realestate.acquisition.application.service`:

- Injiziert `BrokerageContractRepository` und `ApplicationEventPublisher`
- Methode `close(UUID contractId)`:
    1. Lade den BrokerageContract
    2. Rufe `close()` auf (registriert das Event im Aggregate)
    3. Speichere
    4. Lese die Domain Events aus dem Aggregate und publiziere sie über
       `ApplicationEventPublisher`
    5. Lösche die Domain Events im Aggregate

```java

@Service
@Transactional
public class CloseContractUseCase {

    private final BrokerageContractRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    public void close(UUID contractId) {
        BrokerageContract contract = repository.findById(contractId).orElseThrow(...);

        contract.close();
        repository.save(contract);

        contract.getDomainEvents().forEach(eventPublisher::publishEvent);
        contract.clearDomainEvents();
    }

    public BrokerageContract create(UUID ownerId, UUID propertyId,
                                    BigDecimal askingPrice, String currency,
                                    BigDecimal commissionPercentage) {
        // Factory Method aufrufen, speichern und zurückgeben
    }
}
```

## Schritt 4: Event-Listener im Brokerage-BC

Erstelle den Listener `ContractSignedListener` im Package
`de.realestate.brokerage.application.listener`. Der Listener verwendet die
fachlichen Werte aus dem Event (keine hardcodierten Dummy-Werte):

```java

@Component
public class ContractSignedListener {

    private final BrokerageProcessRepository repository;

    @EventListener
    public void handle(ContractSigned event) {
        AskingPrice askingPrice = new AskingPrice(event.askingPrice(), event.currency());
        Commission commission = new Commission(event.commissionPercentage());

        BrokerageProcess process = BrokerageProcess.create(
                event.propertyId(), askingPrice, commission);

        repository.save(process);
    }
}
```

## Schritt 5: REST-Adapter für den Acquisition-BC

Erstelle einen `BrokerageContractController` im Package
`de.realestate.acquisition.adapter.web`:

- `POST /api/acquisition/contracts` – Erstellt einen neuen Vertrag (mit
  askingPrice, currency, commissionPercentage)
- `POST /api/acquisition/contracts/{id}/close` – Schließt den Vertrag ab (
  triggert das Event)

So kann der gesamte Fluss über die API getestet werden:

1. Contract anlegen
2. Contract abschließen
3. BrokerageProcess erscheint automatisch im Brokerage-BC

## Schritt 6: DemoDataInitializer anpassen

Ersetze den bisherigen `Lab07TestDataInitializer` durch einen
`DemoDataInitializer`, der Contracts über den `CloseContractUseCase` anlegt und
abschließt. So entstehen die BrokerageProcesses durch den tatsächlichen
Event-Flow statt durch `reconstitute()`.

## Schritt 7: Test

Schreibe einen Integrationstest, der den gesamten Ablauf verifiziert:

1. Erstelle einen `BrokerageContract` mit konkreten Preis- und Provisionswerten
2. Schließe ihn ab (über den Use Case)
3. Suche den erstellten `BrokerageProcess` gezielt per `propertyId`
4. Prüfe, dass die fachlichen Werte (askingPrice, currency, commission) korrekt
   übernommen wurden

```java

@Test
void shouldCreateBrokerageProcessWhenContractIsSigned() {
    UUID propertyId = UUID.randomUUID();

    BrokerageContract contract = closeContractUseCase.create(
            ownerId, propertyId,
            new BigDecimal("450000.00"), "EUR", new BigDecimal("5.95"));

    closeContractUseCase.close(contract.getId());

    Optional<BrokerageProcess> process =
            brokerageProcessRepository.findByPropertyId(propertyId);

    assertTrue(process.isPresent());
    assertEquals(propertyId, process.get().getPropertyId());
    assertEquals(new BigDecimal("450000.00"), process.get().getAskingPrice().amount());
}
```

## Gut zu wissen

### Event-Publishing: Aggregate vs. Application Service

In dieser Lösung registriert das Aggregate (`BrokerageContract`) das Event
intern in `close()`. Der Application Service liest die Events anschließend aus
und publiziert sie. Das ist konsistent mit dem Muster im Brokerage-BC (
`BrokerageProcess.domainEvents`)
und stellt sicher, dass nur fachlich gültige Events entstehen.

### Shared Kernel und Abhängigkeiten

Der Brokerage-BC importiert das `ContractSigned`-Event direkt aus dem Package
`de.realestate.acquisition.domain.event`. Das erzeugt einen **impliziten Shared
Kernel**
zwischen den beiden Bounded Contexts.

Alternativen wären:

- Ein **separates Shared-Events-Modul**, aus dem beide BCs importieren
- Ein **eigenes Event-Interface im Brokerage-BC** (Anti-Corruption Layer), das
  vom Listener auf die eigene Sprache gemappt wird
- In einem verteilten System: **Serialisierung** (z.B. JSON), sodass keine
  Compile-Time-Abhängigkeit entsteht

Für einen Monolithen ist der direkte Import ein pragmatischer Kompromiss. In
einem verteilten System wäre eine stärkere Entkopplung nötig.

### @EventListener vs. @TransactionalEventListener

Die aktuelle Lösung nutzt `@EventListener`, das synchron in derselben
Transaktion läuft.

**Bonus:** Stelle den Listener auf
`@TransactionalEventListener(phase = AFTER_COMMIT)` um und beobachte, was mit
dem Integrationstest passiert. Was müsste am Test geändert werden, damit er
weiterhin funktioniert?
