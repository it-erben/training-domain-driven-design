package de.realestate.property;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PropertyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PropertyRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM properties");
    }

    @Test
    void createProperty_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Villa am See",
                                    "address": {
                                        "street": "Seestraße 1",
                                        "postalCode": "80331",
                                        "city": "München"
                                    },
                                    "livingArea": 250.0,
                                    "purchasePrice": 750000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Villa am See"))
                .andExpect(jsonPath("$.address.street").value("Seestraße 1"))
                .andExpect(jsonPath("$.address.city").value("München"));
    }

    @Test
    void createProperty_withClientSuppliedStatus_ignoresStatusFromRequest() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Villa am See",
                                    "address": {
                                        "street": "Seestraße 1",
                                        "postalCode": "80331",
                                        "city": "München"
                                    },
                                    "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void findById_existingProperty_returnsOk() throws Exception {
        var property = repository.save(new Property("Altbauwohnung",
                new Address("Hauptstraße 10", "50667", "Köln"),
                new BigDecimal("85.0"), new BigDecimal("320000")));

        mockMvc.perform(get("/api/properties/{id}", property.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Altbauwohnung"))
                .andExpect(jsonPath("$.address.postalCode").value("50667"));
    }

    @Test
    void findById_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/properties/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Property not found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No property with ID 999 exists"))
                .andExpect(jsonPath("$.propertyId").value(999));
    }

    @Test
    void updateProperty_returnsUpdatedData() throws Exception {
        var property = repository.save(new Property("Altes Haus",
                new Address("Bergweg 5", "01234", "Dresden"),
                null, null));

        mockMvc.perform(put("/api/properties/{id}", property.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Renoviertes Haus",
                                    "address": {
                                        "street": "Bergweg 5",
                                        "postalCode": "01234",
                                        "city": "Dresden"
                                    },
                                    "livingArea": 120.0,
                                    "purchasePrice": 400000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renoviertes Haus"))
                .andExpect(jsonPath("$.livingArea").value(120.0));
    }

    @Test
    void updateProperty_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(put("/api/properties/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Egal",
                                    "address": {
                                        "street": "Str. 1",
                                        "postalCode": "12345",
                                        "city": "Berlin"
                                    }
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Property not found"));
    }

    @Test
    void deleteProperty_returnsNoContent() throws Exception {
        var property = repository.save(new Property("Zum Löschen",
                new Address("Teststr. 1", "12345", "Berlin"), null, null));

        mockMvc.perform(delete("/api/properties/{id}", property.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/properties/{id}", property.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProperty_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(delete("/api/properties/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Property not found"));
    }

    @Test
    void createProperty_invalidData_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "", "address": {"street": "", "postalCode": "", "city": ""}}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createProperty_withoutAddress_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/properties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Ohne Adresse"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void publishProperty_returnsActiveStatus() throws Exception {
        var property = repository.save(new Property("Reihenhaus",
                new Address("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("420000")));

        mockMvc.perform(post("/api/properties/{id}/publish", property.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void publishProperty_withCorruptedStoredData_returnsBusinessRuleViolation() throws Exception {
        var property = repository.save(new Property("Reihenhaus",
                new Address("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("420000")));
        jdbcTemplate.update("UPDATE properties SET street = '' WHERE id = ?", property.getId());

        mockMvc.perform(post("/api/properties/{id}/publish", property.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Business Rule Violation"))
                .andExpect(jsonPath("$.detail").value("Property address must be complete for publishing."));
    }

    @Test
    void updateActiveProperty_withLargePriceReduction_resetsStatusToDraft() throws Exception {
        var property = repository.save(new Property("Reihenhaus",
                new Address("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("420000")));
        property.setStatus(PropertyStatus.ACTIVE);
        repository.save(property);

        mockMvc.perform(put("/api/properties/{id}", property.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "title": "Reihenhaus",
                                    "address": {
                                        "street": "Markt 3",
                                        "postalCode": "44135",
                                        "city": "Dortmund"
                                    },
                                    "purchasePrice": 300000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void deleteActiveProperty_returnsBusinessRuleViolation() throws Exception {
        var property = repository.save(new Property("Reihenhaus",
                new Address("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("420000")));
        property.setStatus(PropertyStatus.ACTIVE);
        repository.save(property);

        mockMvc.perform(delete("/api/properties/{id}", property.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Business Rule Violation"));
    }

    @Test
    void retireProperty_returnsRetiredStatus() throws Exception {
        var property = repository.save(new Property("Reihenhaus",
                new Address("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("420000")));
        property.setStatus(PropertyStatus.ACTIVE);
        repository.save(property);

        mockMvc.perform(post("/api/properties/{id}/retire", property.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETIRED"));
    }

    @Test
    void softDelete_recordStillExistsInDatabase() throws Exception {
        var property = repository.save(new Property("Soft Delete Test",
                new Address("Teststr. 1", "12345", "Berlin"), null, null));

        mockMvc.perform(delete("/api/properties/{id}", property.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(property.getId())).isEmpty();

        var count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM properties WHERE id = ? AND deleted = true",
                Integer.class, property.getId());
        assertThat(count).isEqualTo(1);
    }

}
