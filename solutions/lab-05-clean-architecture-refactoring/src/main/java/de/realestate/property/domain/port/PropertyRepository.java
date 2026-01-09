package de.realestate.property.domain.port;

import de.realestate.property.domain.model.Property;

import java.util.List;
import java.util.Optional;

public interface PropertyRepository {

    List<Property> findAll();

    Optional<Property> findById(Long id);

    Property save(Property property);

    boolean existsById(Long id);

    void deleteById(Long id);
}
