package de.realestate.property;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService service;

    public PropertyController(PropertyService service) {
        this.service = service;
    }

    @GetMapping
    public List<Property> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Property findById(@PathVariable Long id) {
        return service.findById(id)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }

    @PostMapping
    public ResponseEntity<Property> create(@Valid @RequestBody Property property) {
        Property saved = service.save(property);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Property update(@PathVariable Long id,
                           @Valid @RequestBody Property property) {
        return service.update(id, property)
                .orElseThrow(() -> new PropertyNotFoundException(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!service.delete(id)) {
            throw new PropertyNotFoundException(id);
        }
    }

}
