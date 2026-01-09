package de.realestate.property;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PropertyService {

    private final PropertyRepository repository;

    public PropertyService(PropertyRepository repository) {
        this.repository = repository;
    }

    public List<Property> findAll() {
        return repository.findAll();
    }

    public Optional<Property> findById(Long id) {
        return repository.findById(id);
    }

    public Property save(Property property) {
        property.setStatus(PropertyStatus.DRAFT);
        return repository.save(property);
    }

    public Optional<Property> update(Long id, Property property) {
        return repository.findById(id)
                .map(existing -> {
                    // Preis-Schutz Logik
                    if (existing.getStatus() == PropertyStatus.ACTIVE && property.getPurchasePrice() != null) {
                        BigDecimal oldPrice = existing.getPurchasePrice();
                        BigDecimal newPrice = property.getPurchasePrice();
                        if (oldPrice != null) {
                            BigDecimal threshold = oldPrice.multiply(new BigDecimal("0.80"));
                            if (newPrice.compareTo(threshold) < 0) {
                                existing.setStatus(PropertyStatus.DRAFT);
                            }
                        }
                    }

                    existing.setTitle(property.getTitle());
                    existing.setAddress(property.getAddress());
                    existing.setLivingArea(property.getLivingArea());
                    existing.setPurchasePrice(property.getPurchasePrice());
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        return repository.findById(id)
                .map(property -> {
                    if (property.getStatus() == PropertyStatus.ACTIVE) {
                        throw new PropertyBusinessRuleException("An active property cannot be deleted. Retire it first.");
                    }
                    repository.delete(property);
                    return true;
                }).orElse(false);
    }

    public Property publish(Long id) {
        Property property = repository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));

        if (property.getTitle() == null || property.getTitle().isBlank()) {
            throw new PropertyBusinessRuleException("Property title must not be empty for publishing.");
        }
        if (property.getAddress() == null ||
                property.getAddress().street() == null || property.getAddress().street().isBlank() ||
                property.getAddress().postalCode() == null || property.getAddress().postalCode().isBlank() ||
                property.getAddress().city() == null || property.getAddress().city().isBlank()) {
            throw new PropertyBusinessRuleException("Property address must be complete for publishing.");
        }

        property.setStatus(PropertyStatus.ACTIVE);
        return repository.save(property);
    }

    public Property retire(Long id) {
        Property property = repository.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));
        property.setStatus(PropertyStatus.RETIRED);
        return repository.save(property);
    }
}
