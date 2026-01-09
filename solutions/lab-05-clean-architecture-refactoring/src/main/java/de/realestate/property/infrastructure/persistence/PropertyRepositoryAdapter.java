package de.realestate.property.infrastructure.persistence;

import de.realestate.property.domain.model.Property;
import de.realestate.property.domain.port.PropertyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PropertyRepositoryAdapter implements PropertyRepository {

    private final JpaPropertyRepository jpaRepository;

    public PropertyRepositoryAdapter(JpaPropertyRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<Property> findAll() {
        return jpaRepository.findAll().stream()
                .map(JpaProperty::toModel)
                .toList();
    }

    @Override
    public Optional<Property> findById(Long id) {
        return jpaRepository.findById(id)
                .map(JpaProperty::toModel);
    }

    @Override
    public Property save(Property property) {
        JpaProperty jpaEntity = JpaProperty.fromModel(property);
        JpaProperty saved = jpaRepository.save(jpaEntity);
        return saved.toModel();
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
