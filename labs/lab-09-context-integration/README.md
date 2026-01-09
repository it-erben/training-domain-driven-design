# Lab 09: Context-Integration - Bounded Contexts verbinden

Erstelle einen zweiten Bounded Context "Akquise" (Acquisition) und verbinde ihn über Domain Events mit dem bestehenden Brokerage-BC.

## Schritt 1: Minimalen Acquisition-BC erstellen

Erstelle die Entity `BrokerageContract` im Package `de.realestate.acquisition.domain.model`:

- Felder: `id` (UUID), `ownerId` (UUID), `propertyId` (UUID), `closedAt` (LocalDateTime)
- Methode: `close()` setzt `closedAt` auf den aktuellen Zeitstempel

```java
public class BrokerageContract {

    private final UUID id;
    private final UUID ownerId;
    private final UUID propertyId;
    private LocalDateTime closedAt;

    // Constructor, factory method, getters
    // close() sets closedAt = LocalDateTime.now()
}
```

## Schritt 2: Integrations-Event erstellen

Erstelle das Integrations-Event `ContractSigned` als Record im Package `de.realestate.acquisition.domain.event`:

```java
public record ContractSigned(
    UUID contractId,
    UUID propertyId,
    LocalDateTime closedAt
) {}
```

## Schritt 3: Application Service im Acquisition-BC

Erstelle den Service `CloseContractUseCase` im Package `de.realestate.acquisition.application.service`:

- Injiziert `BrokerageContractRepository` und `ApplicationEventPublisher`
- Methode `close(UUID contractId)`:
  1. Lade den BrokerageContract
  2. Rufe `close()` auf
  3. Speichere
  4. Veröffentliche das `ContractSigned`-Event über `ApplicationEventPublisher`

```java
@Service
public class CloseContractUseCase {

    private final BrokerageContractRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    // Constructor Injection

    @Transactional
    public void close(UUID contractId) {
        // 1. Load
        // 2. close()
        // 3. Save
        // 4. Publish event
    }
}
```

## Schritt 4: Event-Listener im Brokerage-BC

Erstelle den Listener `ContractSignedListener` im Package `de.realestate.brokerage.application.listener`:

```java
@Component
public class ContractSignedListener {

    private final BrokerageProcessRepository repository;

    // Constructor Injection

    @EventListener
    public void handle(ContractSigned event) {
        // Create a new BrokerageProcess
        // using the propertyId from the event
        // Save
    }
}
```

## Schritt 5: Test

Schreibe einen Integrationstest, der den gesamten Ablauf verifiziert:

1. Erstelle einen `BrokerageContract`
2. Schließe ihn ab (über den Use Case)
3. Überprüfe, dass ein `BrokerageProcess` automatisch erstellt wurde

## Gut zu wissen:

- Springs `ApplicationEventPublisher` eignet sich gut für die Kommunikation zwischen Bounded Contexts innerhalb eines Monolithen.
- Das Event gehört zum publizierenden BC (Acquisition) - der konsumierende BC (Brokerage) importiert es.
- Stelle sicher, dass der Listener im Brokerage-BC keine direkte Abhängigkeit zum Acquisition-Domain-Modell hat - nur zum Event.
- `@TransactionalEventListener(phase = AFTER_COMMIT)` stellt sicher, dass das Event nur verarbeitet wird, wenn die Transaktion erfolgreich war.
