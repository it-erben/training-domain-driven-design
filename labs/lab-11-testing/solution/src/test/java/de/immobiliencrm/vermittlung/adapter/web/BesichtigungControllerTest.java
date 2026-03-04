package de.immobiliencrm.vermittlung.adapter.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.application.service.BesichtigungAnlegenUseCase;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer test using @WebMvcTest -- only the controller and its dependencies are loaded.
 * The use case is mocked via @MockBean.
 */
@WebMvcTest(BesichtigungController.class)
class BesichtigungControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BesichtigungAnlegenUseCase useCase;

    @Test
    @DisplayName("POST with valid body returns 201 Created")
    void test_besichtigungAnlegen_gueltigerRequest_201() throws Exception {
        // Arrange
        UUID vorgangId = UUID.randomUUID();
        UUID besichtigungId = UUID.randomUUID();

        when(useCase.anlegen(any(BesichtigungAnlegenCommand.class)))
                .thenReturn(new BesichtigungAnlegenResult(besichtigungId, vorgangId));

        String requestBody = """
                {
                    "interessentName": "Erika Musterfrau",
                    "zeitpunkt": "2025-07-01T10:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{id}/besichtigungen", vorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.besichtigungId").value(besichtigungId.toString()))
                .andExpect(jsonPath("$.vermittlungsvorgangId").value(vorgangId.toString()));
    }

    @Test
    @DisplayName("POST with invalid body returns 422 Unprocessable Entity")
    void test_besichtigungAnlegen_ungueltigerRequest_422() throws Exception {
        // Arrange: empty body with missing required fields
        UUID vorgangId = UUID.randomUUID();

        String requestBody = """
                {
                    "interessentName": "",
                    "zeitpunkt": null
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{id}/besichtigungen", vorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST with unknown vorgangId returns 404 Not Found")
    void test_besichtigungAnlegen_vorgangNichtGefunden_404() throws Exception {
        // Arrange
        UUID unknownVorgangId = UUID.randomUUID();

        when(useCase.anlegen(any(BesichtigungAnlegenCommand.class)))
                .thenThrow(new VermittlungsvorgangNichtGefundenException(unknownVorgangId));

        String requestBody = """
                {
                    "interessentName": "Max Mustermann",
                    "zeitpunkt": "2025-07-01T10:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{id}/besichtigungen", unknownVorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }
}
