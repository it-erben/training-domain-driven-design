package de.foerderung.antragstellung.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenCommand;
import de.foerderung.antragstellung.application.command.FlurstueckHinzufuegenResult;
import de.foerderung.antragstellung.application.command.FlurstueckPruefenCommand;
import de.foerderung.antragstellung.application.service.FlurstueckHinzufuegenService;
import de.foerderung.antragstellung.application.service.FlurstueckPruefenService;
import de.foerderung.antragstellung.application.service.FlurstueckeAbfragenUseCase;
import de.foerderung.antragstellung.domain.model.AntragsmappeNichtGefundenException;
import de.foerderung.antragstellung.domain.model.Flurstueck;
import de.foerderung.antragstellung.domain.model.FlurstueckNummer;

@WebMvcTest(FlurstueckController.class)
class FlurstueckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FlurstueckHinzufuegenService hinzufuegenService;

    @MockitoBean
    private FlurstueckPruefenService pruefenService;

    @MockitoBean
    private FlurstueckeAbfragenUseCase abfragenUseCase;

    @Test
    void should_return201WithLocationHeader() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();
        UUID flurstueckId = UUID.randomUUID();

        FlurstueckHinzufuegenResult result = new FlurstueckHinzufuegenResult(flurstueckId, antragsmappeId);
        when(hinzufuegenService.hinzufuegen(any(FlurstueckHinzufuegenCommand.class)))
                .thenReturn(result);

        String requestBody = """
                {
                    "flurstueckNummer": "012-00345-00678",
                    "flaeche": 12.50
                }
                """;

        mockMvc.perform(post("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke", antragsmappeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/antragstellung/antragsmappen/" + antragsmappeId + "/flurstuecke/" + flurstueckId))
                .andExpect(jsonPath("$.flurstueckId").value(flurstueckId.toString()))
                .andExpect(jsonPath("$.antragsmappeId").value(antragsmappeId.toString()));
    }

    @Test
    void should_return404WhenAntragsmappeNotFound() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();

        when(hinzufuegenService.hinzufuegen(any(FlurstueckHinzufuegenCommand.class)))
                .thenThrow(new AntragsmappeNichtGefundenException(antragsmappeId));

        String requestBody = """
                {
                    "flurstueckNummer": "012-00345-00678",
                    "flaeche": 12.50
                }
                """;

        mockMvc.perform(post("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke", antragsmappeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("AntragsMappe mit ID " + antragsmappeId + " nicht gefunden"));
    }

    @Test
    void should_return400WhenValidationFails() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();

        String requestBody = """
                {
                    "flurstueckNummer": "",
                    "flaeche": null
                }
                """;

        mockMvc.perform(post("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke", antragsmappeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed for request body."))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.field=='flurstueckNummer')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='flaeche')].message").exists());
    }

    @Test
    void should_listFlurstueckeForAntragsmappe() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();

        Flurstueck flurstueck = Flurstueck.rekonstruieren(
                UUID.randomUUID(),
                new FlurstueckNummer("012-00345-00678"),
                new BigDecimal("12.50"),
                "Ackerland Nordfeld",
                false);

        when(abfragenUseCase.abfragen(antragsmappeId)).thenReturn(List.of(flurstueck));

        mockMvc.perform(get("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke", antragsmappeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nummer").value("012-00345-00678"))
                .andExpect(jsonPath("$[0].flaeche").value(12.50))
                .andExpect(jsonPath("$[0].bemerkung").value("Ackerland Nordfeld"))
                .andExpect(jsonPath("$[0].geprueft").value(false));
    }

    @Test
    void should_pruefenFlurstueck() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();
        UUID flurstueckId = UUID.randomUUID();

        mockMvc.perform(patch("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke/{flurstueckId}/pruefen",
                        antragsmappeId, flurstueckId))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return404WhenListingFlurstueckeForUnknownAntragsmappe() throws Exception {
        UUID antragsmappeId = UUID.randomUUID();

        when(abfragenUseCase.abfragen(antragsmappeId))
                .thenThrow(new AntragsmappeNichtGefundenException(antragsmappeId));

        mockMvc.perform(get("/api/antragstellung/antragsmappen/{antragsmappeId}/flurstuecke", antragsmappeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.foerderung.de/errors/antragsmappe-not-found"));
    }
}
