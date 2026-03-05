package de.realestate.brokerage.adapter.web;

import tools.jackson.databind.ObjectMapper;
import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
 * The use case is mocked via @MockitoBean.
 */
@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateViewingUseCase useCase;

    @Test
    @DisplayName("POST with valid body returns 201 Created")
    void test_createViewing_validRequest_201() throws Exception {
        // Arrange
        UUID processId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        when(useCase.createViewing(any(CreateViewingCommand.class)))
                .thenReturn(new CreateViewingResult(viewingId, processId));

        String requestBody = """
                {
                    "prospectName": "Erika Musterfrau",
                    "appointmentDate": "2025-07-01T10:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage-processes/{id}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.viewingId").value(viewingId.toString()))
                .andExpect(jsonPath("$.processId").value(processId.toString()));
    }

    @Test
    @DisplayName("POST with invalid body returns 422 Unprocessable Entity")
    void test_createViewing_invalidRequest_422() throws Exception {
        // Arrange: empty body with missing required fields
        UUID processId = UUID.randomUUID();

        String requestBody = """
                {
                    "prospectName": "",
                    "appointmentDate": null
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage-processes/{id}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST with unknown processId returns 404 Not Found")
    void test_createViewing_processNotFound_404() throws Exception {
        // Arrange
        UUID unknownProcessId = UUID.randomUUID();

        when(useCase.createViewing(any(CreateViewingCommand.class)))
                .thenThrow(new ProcessNotFoundException(unknownProcessId));

        String requestBody = """
                {
                    "prospectName": "Max Mustermann",
                    "appointmentDate": "2025-07-01T10:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage-processes/{id}/viewings", unknownProcessId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }
}
