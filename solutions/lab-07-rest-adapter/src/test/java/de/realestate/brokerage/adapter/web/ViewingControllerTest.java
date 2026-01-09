package de.realestate.brokerage.adapter.web;

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
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import de.realestate.brokerage.application.command.CreateViewingCommand;
import de.realestate.brokerage.application.command.CreateViewingResult;
import de.realestate.brokerage.application.command.CompleteViewingCommand;
import de.realestate.brokerage.application.service.CompleteViewingUseCase;
import de.realestate.brokerage.application.service.CreateViewingUseCase;
import de.realestate.brokerage.application.service.ListViewingsUseCase;
import de.realestate.brokerage.domain.model.*;

@WebMvcTest(ViewingController.class)
class ViewingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateViewingUseCase createViewingUseCase;

    @MockitoBean
    private CompleteViewingUseCase completeViewingUseCase;

    @MockitoBean
    private ListViewingsUseCase listViewingsUseCase;

    @Test
    void should_return201WithLocationHeader() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        CreateViewingResult result = new CreateViewingResult(viewingId, processId);
        when(createViewingUseCase.create(any(CreateViewingCommand.class)))
                .thenReturn(result);

        String requestBody = """
                {
                    "prospectName": "Max Mustermann",
                    "appointmentDate": "2099-04-01T14:00:00"
                }
                """;

        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        "/api/brokerage/processes/" + processId + "/viewings/" + viewingId))
                .andExpect(jsonPath("$.viewingId").value(viewingId.toString()))
                .andExpect(jsonPath("$.processId").value(processId.toString()));
    }

    @Test
    void should_return404WhenProcessNotFound() throws Exception {
        UUID processId = UUID.randomUUID();

        when(createViewingUseCase.create(any(CreateViewingCommand.class)))
                .thenThrow(new ProcessNotFoundException(processId));

        String requestBody = """
                {
                    "prospectName": "Max Mustermann",
                    "appointmentDate": "2099-04-01T14:00:00"
                }
                """;

        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("BrokerageProcess with ID " + processId + " not found"));
    }

    @Test
    void should_return400WhenValidationFails() throws Exception {
        UUID processId = UUID.randomUUID();

        String requestBody = """
                {
                    "prospectName": "",
                    "appointmentDate": null
                }
                """;

        mockMvc.perform(post("/api/brokerage/processes/{processId}/viewings", processId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value("Validation failed for request body."))
                .andExpect(jsonPath("$.errors.length()").value(2))
                .andExpect(jsonPath("$.errors[?(@.field=='prospectName')].message").exists())
                .andExpect(jsonPath("$.errors[?(@.field=='appointmentDate')].message").exists());
    }

    @Test
    void should_listViewingsForProcess() throws Exception {
        UUID processId = UUID.randomUUID();
        BrokerageProcess process = BrokerageProcess.create(
                UUID.randomUUID(),
                new AskingPrice(BigDecimal.valueOf(500000), "EUR"),
                new Commission(BigDecimal.valueOf(3.57)));
        process.addViewing("Max Mustermann", LocalDateTime.of(2099, 4, 1, 14, 0));

        when(listViewingsUseCase.list(processId)).thenReturn(process.getViewings());

        mockMvc.perform(get("/api/brokerage/processes/{processId}/viewings", processId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].prospectName").value("Max Mustermann"));
    }

    @Test
    void should_completeViewing() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        mockMvc.perform(patch("/api/brokerage/processes/{processId}/viewings/{viewingId}/complete",
                        processId, viewingId))
                .andExpect(status().isNoContent());
    }

    @Test
    void should_return404WhenViewingNotFoundDuringCompletion() throws Exception {
        UUID processId = UUID.randomUUID();
        UUID viewingId = UUID.randomUUID();

        doThrow(new ViewingNotFoundException(processId, viewingId))
                .when(completeViewingUseCase)
                .complete(any(CompleteViewingCommand.class));

        mockMvc.perform(patch("/api/brokerage/processes/{processId}/viewings/{viewingId}/complete",
                        processId, viewingId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.immo-crm.de/errors/viewing-not-found"))
                .andExpect(jsonPath("$.detail").value(
                        "Viewing with ID " + viewingId + " not found in BrokerageProcess " + processId));
    }

    @Test
    void should_return404WhenListingViewingsForUnknownProcess() throws Exception {
        UUID processId = UUID.randomUUID();

        when(listViewingsUseCase.list(processId)).thenThrow(new ProcessNotFoundException(processId));

        mockMvc.perform(get("/api/brokerage/processes/{processId}/viewings", processId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.type").value("https://api.immo-crm.de/errors/process-not-found"));
    }
}
