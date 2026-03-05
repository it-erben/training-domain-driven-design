package de.realestate.persistence;

import de.realestate.brokerage.infrastructure.persistence.JpaBrokerageProcess;
import de.realestate.brokerage.infrastructure.persistence.JpaBrokerageProcessRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test verifying that JPA auditing fields are populated on save.
 */
@SpringBootTest
class JpaAuditingTest {

    @Autowired
    private JpaBrokerageProcessRepository repository;

    @Test
    void should_set_created_date_on_save() {
        // Given: a new JPA entity
        JpaBrokerageProcess entity = new JpaBrokerageProcess(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Teststrasse 1",
                "12345",
                "Berlin",
                new BigDecimal("500000"),
                "EUR",
                new BigDecimal("3.57"),
                "NEU"
        );

        // When: saved to the database
        JpaBrokerageProcess saved = repository.saveAndFlush(entity);

        // Then: audit fields should be populated
        assertNotNull(saved.getCreatedDate(), "createdDate should be set after save");
        assertNotNull(saved.getLastModifiedDate(), "lastModifiedDate should be set after save");
        assertEquals("system", saved.getCreatedBy(), "createdBy should be 'system'");
    }
}
