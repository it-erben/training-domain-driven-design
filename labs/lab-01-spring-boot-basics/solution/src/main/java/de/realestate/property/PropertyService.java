package de.realestate.property;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
        return repository.save(property);
    }

    public Optional<Property> update(Long id, Property property) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setTitle(property.getTitle());
                    existing.setStreet(property.getStreet());
                    existing.setPostalCode(property.getPostalCode());
                    existing.setCity(property.getCity());
                    existing.setLivingArea(property.getLivingArea());
                    existing.setPurchasePrice(property.getPurchasePrice());
                    return repository.save(existing);
                });
    }

    public boolean delete(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

}
