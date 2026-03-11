package de.foerderung.betriebsinhaber;

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
class BetriebsinhaberControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BetriebsinhaberRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM betriebsinhaber");
    }

    @Test
    void createBetriebsinhaber_returnsCreated() throws Exception {
        mockMvc.perform(post("/api/betriebsinhaber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Hans Mueller",
                                    "betriebsnummer": "DE-BY-001234",
                                    "adresse": {
                                        "strasse": "Hofweg 12",
                                        "plz": "80331",
                                        "ort": "Muenchen"
                                    },
                                    "betriebsflaeche": 250.0,
                                    "foerdersumme": 75000
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Hans Mueller"))
                .andExpect(jsonPath("$.adresse.strasse").value("Hofweg 12"))
                .andExpect(jsonPath("$.adresse.ort").value("Muenchen"));
    }

    @Test
    void createBetriebsinhaber_withClientSuppliedStatus_ignoresStatusFromRequest() throws Exception {
        mockMvc.perform(post("/api/betriebsinhaber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Hans Mueller",
                                    "betriebsnummer": "DE-BY-001234",
                                    "adresse": {
                                        "strasse": "Hofweg 12",
                                        "plz": "80331",
                                        "ort": "Muenchen"
                                    },
                                    "status": "AKTIV"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ENTWURF"));
    }

    @Test
    void findById_existingBetriebsinhaber_returnsOk() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Anna Schmidt",
                "DE-NW-005678",
                new Betriebsadresse("Hauptstrasse 10", "50667", "Koeln"),
                new BigDecimal("85.0"), new BigDecimal("32000")));

        mockMvc.perform(get("/api/betriebsinhaber/{id}", betriebsinhaber.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna Schmidt"))
                .andExpect(jsonPath("$.adresse.plz").value("50667"));
    }

    @Test
    void findById_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(get("/api/betriebsinhaber/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Betriebsinhaber nicht gefunden"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Kein Betriebsinhaber mit ID 999 gefunden"))
                .andExpect(jsonPath("$.betriebsinhaberId").value(999));
    }

    @Test
    void updateBetriebsinhaber_returnsUpdatedData() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Alter Name",
                "DE-SN-009999",
                new Betriebsadresse("Bergweg 5", "01234", "Dresden"),
                null, null));

        mockMvc.perform(put("/api/betriebsinhaber/{id}", betriebsinhaber.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Neuer Name",
                                    "betriebsnummer": "DE-SN-009999",
                                    "adresse": {
                                        "strasse": "Bergweg 5",
                                        "plz": "01234",
                                        "ort": "Dresden"
                                    },
                                    "betriebsflaeche": 120.0,
                                    "foerdersumme": 40000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Neuer Name"))
                .andExpect(jsonPath("$.betriebsflaeche").value(120.0));
    }

    @Test
    void updateBetriebsinhaber_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(put("/api/betriebsinhaber/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Egal",
                                    "betriebsnummer": "DE-XX-000000",
                                    "adresse": {
                                        "strasse": "Str. 1",
                                        "plz": "12345",
                                        "ort": "Berlin"
                                    }
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Betriebsinhaber nicht gefunden"));
    }

    @Test
    void deleteBetriebsinhaber_returnsNoContent() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Zum Loeschen",
                "DE-BE-000001",
                new Betriebsadresse("Teststr. 1", "12345", "Berlin"), null, null));

        mockMvc.perform(delete("/api/betriebsinhaber/{id}", betriebsinhaber.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/betriebsinhaber/{id}", betriebsinhaber.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteBetriebsinhaber_nonExistent_returnsProblemDetail() throws Exception {
        mockMvc.perform(delete("/api/betriebsinhaber/{id}", 999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Betriebsinhaber nicht gefunden"));
    }

    @Test
    void createBetriebsinhaber_invalidData_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/betriebsinhaber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "", "betriebsnummer": "", "adresse": {"strasse": "", "plz": "", "ort": ""}}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBetriebsinhaber_withoutAdresse_returnsValidationError() throws Exception {
        mockMvc.perform(post("/api/betriebsinhaber")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Ohne Adresse", "betriebsnummer": "DE-XX-000000"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void freigebenBetriebsinhaber_returnsAktivStatus() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Klaus Bauer",
                "DE-NW-004444",
                new Betriebsadresse("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("42000")));

        mockMvc.perform(post("/api/betriebsinhaber/{id}/freigeben", betriebsinhaber.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AKTIV"));
    }

    @Test
    void freigebenBetriebsinhaber_withCorruptedStoredData_returnsBusinessRuleViolation() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Klaus Bauer",
                "DE-NW-004444",
                new Betriebsadresse("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("42000")));
        jdbcTemplate.update("UPDATE betriebsinhaber SET strasse = '' WHERE id = ?", betriebsinhaber.getId());

        mockMvc.perform(post("/api/betriebsinhaber/{id}/freigeben", betriebsinhaber.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Fachliche Regel verletzt"))
                .andExpect(jsonPath("$.detail").value("Adresse muss vollstaendig sein fuer die Freigabe."));
    }

    @Test
    void updateAktiverBetriebsinhaber_withLargeFlaecheReduction_resetsStatusToEntwurf() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Klaus Bauer",
                "DE-NW-004444",
                new Betriebsadresse("Markt 3", "44135", "Dortmund"),
                new BigDecimal("420"), new BigDecimal("42000")));
        betriebsinhaber.setStatus(BetriebsinhaberStatus.AKTIV);
        repository.save(betriebsinhaber);

        mockMvc.perform(put("/api/betriebsinhaber/{id}", betriebsinhaber.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Klaus Bauer",
                                    "betriebsnummer": "DE-NW-004444",
                                    "adresse": {
                                        "strasse": "Markt 3",
                                        "plz": "44135",
                                        "ort": "Dortmund"
                                    },
                                    "betriebsflaeche": 300
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTWURF"));
    }

    @Test
    void deleteAktiverBetriebsinhaber_returnsBusinessRuleViolation() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Klaus Bauer",
                "DE-NW-004444",
                new Betriebsadresse("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("42000")));
        betriebsinhaber.setStatus(BetriebsinhaberStatus.AKTIV);
        repository.save(betriebsinhaber);

        mockMvc.perform(delete("/api/betriebsinhaber/{id}", betriebsinhaber.getId()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Fachliche Regel verletzt"));
    }

    @Test
    void stilllegenBetriebsinhaber_returnsStillgelegtStatus() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Klaus Bauer",
                "DE-NW-004444",
                new Betriebsadresse("Markt 3", "44135", "Dortmund"),
                null, new BigDecimal("42000")));
        betriebsinhaber.setStatus(BetriebsinhaberStatus.AKTIV);
        repository.save(betriebsinhaber);

        mockMvc.perform(post("/api/betriebsinhaber/{id}/stilllegen", betriebsinhaber.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STILLGELEGT"));
    }

    @Test
    void softDelete_recordStillExistsInDatabase() throws Exception {
        var betriebsinhaber = repository.save(new Betriebsinhaber("Soft Delete Test",
                "DE-BE-000002",
                new Betriebsadresse("Teststr. 1", "12345", "Berlin"), null, null));

        mockMvc.perform(delete("/api/betriebsinhaber/{id}", betriebsinhaber.getId()))
                .andExpect(status().isNoContent());

        assertThat(repository.findById(betriebsinhaber.getId())).isEmpty();

        var count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM betriebsinhaber WHERE id = ? AND deleted = true",
                Integer.class, betriebsinhaber.getId());
        assertThat(count).isEqualTo(1);
    }
}
