package de.immobiliencrm.vermittlung.adapter.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenCommand;
import de.immobiliencrm.vermittlung.application.command.BesichtigungAnlegenResult;
import de.immobiliencrm.vermittlung.application.service.BesichtigungAnlegenUseCase;
import de.immobiliencrm.vermittlung.domain.model.VermittlungsvorgangNichtGefundenException;

@WebMvcTest(BesichtigungController.class)
class BesichtigungControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BesichtigungAnlegenUseCase besichtigungAnlegenUseCase;

    @Test
    void anlegen_shouldReturn201WithLocationHeader() throws Exception {
        // Arrange
        UUID vorgangId = UUID.randomUUID();
        UUID besichtigungId = UUID.randomUUID();

        BesichtigungAnlegenResult result = new BesichtigungAnlegenResult(besichtigungId, vorgangId);
        when(besichtigungAnlegenUseCase.anlegen(any(BesichtigungAnlegenCommand.class)))
                .thenReturn(result);

        String requestBody = """
                {
                    "interessentName": "Max Mustermann",
                    "zeitpunkt": "2025-04-01T14:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen", vorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.besichtigungId").value(besichtigungId.toString()))
                .andExpect(jsonPath("$.vermittlungsvorgangId").value(vorgangId.toString()));
    }

    @Test
    void anlegen_shouldReturn404WhenVorgangNotFound() throws Exception {
        // Arrange
        UUID vorgangId = UUID.randomUUID();

        when(besichtigungAnlegenUseCase.anlegen(any(BesichtigungAnlegenCommand.class)))
                .thenThrow(new VermittlungsvorgangNichtGefundenException(vorgangId));

        String requestBody = """
                {
                    "interessentName": "Max Mustermann",
                    "zeitpunkt": "2025-04-01T14:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen", vorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Vermittlungsvorgang mit ID " + vorgangId + " nicht gefunden"));
    }

    @Test
    void anlegen_shouldReturn422WhenValidationFails() throws Exception {
        // Arrange
        UUID vorgangId = UUID.randomUUID();

        String requestBody = """
                {
                    "interessentName": "",
                    "zeitpunkt": null
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/vermittlungsvorgaenge/{vorgangId}/besichtigungen", vorgangId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(422));
    }
}
