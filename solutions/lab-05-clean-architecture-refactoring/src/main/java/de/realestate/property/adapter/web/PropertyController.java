package de.realestate.property.adapter.web;

import de.realestate.property.application.service.PropertyService;
import de.realestate.property.domain.model.Property;
import de.realestate.property.domain.model.PropertyAddress;
import de.realestate.property.domain.model.PropertyNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @GetMapping
    public List<PropertyResponse> findAll() {
        return service.findAll().stream()
                .map(PropertyResponse::fromModel)
                .toList();
    }

    @GetMapping("/{id}")
    public PropertyResponse findById(@PathVariable Long id) {
        return service.findById(id)
                .map(PropertyResponse::fromModel)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<PropertyResponse> create(@Valid @RequestBody PropertyRequest request) {
        Property property = request.toModel();
        Property saved = service.save(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(PropertyResponse.fromModel(saved));
    }

    @PutMapping("/{id}")
    public PropertyResponse update(@PathVariable Long id,
                                   @Valid @RequestBody PropertyRequest request) {
        return service.update(id, request.toModel())
                .map(PropertyResponse::fromModel)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            throw new PropertyNotFoundException(id);
        }
    }

    record PropertyRequest(
            @NotBlank String title,
            @Valid AddressRequest address,
            BigDecimal livingArea,
            BigDecimal purchasePrice
    ) {
        Property toModel() {
            PropertyAddress addr = address != null
                    ? new PropertyAddress(address.street(), address.postalCode(), address.city())
                    : null;
            return Property.create(title, addr, livingArea, purchasePrice);
        }
    }

    record AddressRequest(
            @NotBlank String street,
            @NotBlank String postalCode,
            @NotBlank String city
    ) {}

    record PropertyResponse(
            Long id,
            String title,
            AddressResponse address,
            BigDecimal livingArea,
            BigDecimal purchasePrice
    ) {
        static PropertyResponse fromModel(Property property) {
            AddressResponse addr = null;
            if (property.getAddress() != null) {
                addr = new AddressResponse(
                        property.getAddress().street(),
                        property.getAddress().postalCode(),
                        property.getAddress().city()
                );
            }
            return new PropertyResponse(
                    property.getId(),
                    property.getTitle(),
                    addr,
                    property.getLivingArea(),
                    property.getPurchasePrice()
            );
        }
    }

    record AddressResponse(String street, String postalCode, String city) {}
}
