package de.realestate.acquisition.adapter.web;

import de.realestate.acquisition.application.service.CloseContractUseCase;
import de.realestate.acquisition.domain.model.BrokerageContract;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

/**
 * REST adapter for the Acquisition bounded context.
 */
@RestController
@RequestMapping("/api/acquisition/contracts")
public class BrokerageContractController {

    private final CloseContractUseCase closeContractUseCase;

    public BrokerageContractController(CloseContractUseCase closeContractUseCase) {
        this.closeContractUseCase = closeContractUseCase;
    }

    @PostMapping
    public ResponseEntity<CreateContractResponse> create(@RequestBody CreateContractRequest request) {
        BrokerageContract contract = closeContractUseCase.create(
                request.ownerId(), request.propertyId(),
                request.askingPrice(), request.currency(),
                request.commissionPercentage());

        CreateContractResponse response = new CreateContractResponse(
                contract.getId(), contract.getOwnerId(), contract.getPropertyId(),
                contract.getAskingPrice(), contract.getCurrency(),
                contract.getCommissionPercentage());

        return ResponseEntity
                .created(URI.create("/api/acquisition/contracts/" + contract.getId()))
                .body(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<Void> close(@PathVariable UUID id) {
        closeContractUseCase.close(id);
        return ResponseEntity.noContent().build();
    }
}
