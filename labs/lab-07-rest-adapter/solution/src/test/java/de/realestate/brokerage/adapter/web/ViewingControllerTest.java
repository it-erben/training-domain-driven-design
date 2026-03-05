package de.realestate.brokerage.adapter.web;

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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
import de.realestate.brokerage.domain.model.ProcessNotFoundException;

@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateViewingUseCase createViewingUseCase;

    @Test
    void should_return201WithLocationHeader() throws Exception {
        // Arrange
        UUID processId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        CreateViewingResult result = new CreateViewingResult(viewingId, processId);
        when(createViewingUseCase.create(any(CreateViewingCommand.class)))
                .thenReturn(result);

        String requestBody = """
                {
                    "prospectName": "Max Mustermann",
                    "appointmentDate": "2025-04-01T14:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.viewingId").value(viewingId.toString()))
                .andExpect(jsonPath("$.processId").value(processId.toString()));
    }

    @Test
    void should_return404WhenProcessNotFound() throws Exception {
        // Arrange
        UUID processId = UUID.randomUUID();

        when(createViewingUseCase.create(any(CreateViewingCommand.class)))
                .thenThrow(new ProcessNotFoundException(processId));

        String requestBody = """
                {
                    "prospectName": "Max Mustermann",
                    "appointmentDate": "2025-04-01T14:00:00"
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("BrokerageProcess with ID " + processId + " not found"));
    }

    @Test
    void should_return422WhenValidationFails() throws Exception {
        // Arrange
        UUID processId = UUID.randomUUID();

        String requestBody = """
                {
                    "prospectName": "",
                    "appointmentDate": null
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(422));
    }
}
