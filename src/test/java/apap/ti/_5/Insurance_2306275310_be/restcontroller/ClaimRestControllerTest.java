package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.ClaimService;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ClaimRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClaimService claimService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClaimSummaryResponseDTO claimSummaryDTO;
    private ClaimDetailResponseDTO claimDetailDTO;
    private CreateClaimRequestDTO createClaimDTO;
    private ProcessClaimRequestDTO processClaimDTO;

    @BeforeEach
    void setUp() {
        claimSummaryDTO = ClaimSummaryResponseDTO.builder()
                .id("CLM1")
                .orderedPlanId("OP1")
                .planName("Test Plan")
                .status("WAITING_FOR_REVIEW")
                .daysSinceClaimed(1)
                .build();

        claimDetailDTO = ClaimDetailResponseDTO.builder()
                .id("CLM1")
                .status("WAITING_FOR_REVIEW")
                .proof("proof.jpg")
                .build();
                
        createClaimDTO = new CreateClaimRequestDTO("new_proof.jpg");
        
        processClaimDTO = ProcessClaimRequestDTO.builder()
                .isAccepted(true)
                .acceptedNote("Looks good.")
                .build();
    }

    @Test
    void testGetAllClaimsFiltered_Success() throws Exception {
        when(claimService.getAllClaimsFiltered(any(), any())).thenReturn(List.of(claimSummaryDTO));

        mockMvc.perform(get("/api/claim")
                .param("status", "WAITING_FOR_REVIEW")
                .param("planId", "INS1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Claim data retrieved successfully."))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value("CLM1"));
        
        verify(claimService, times(1)).getAllClaimsFiltered("WAITING_FOR_REVIEW", "INS1");
    }

    @Test
    void testGetClaimById_Success() throws Exception {
        when(claimService.getClaimById("CLM1")).thenReturn(claimDetailDTO);

        mockMvc.perform(get("/api/claim/CLM1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("CLM1"));
    }

    @Test
    void testGetClaimById_NotFound() throws Exception {
        when(claimService.getClaimById("CLM99")).thenThrow(new RuntimeException("Claim not found"));

        mockMvc.perform(get("/api/claim/CLM99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Claim not found"));
    }

    @Test
    void testSubmitClaim_Success() throws Exception {
        when(claimService.createClaim(eq("OP1"), any(CreateClaimRequestDTO.class))).thenReturn(claimDetailDTO);

        mockMvc.perform(post("/api/claim/submit/OP1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createClaimDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Claim submitted successfully."))
                .andExpect(jsonPath("$.data.id").value("CLM1"));
    }

    @Test
    void testSubmitClaim_ValidationError() throws Exception {
        // Invalid DTO (proof is blank)
        CreateClaimRequestDTO invalidDTO = new CreateClaimRequestDTO(" ");

        mockMvc.perform(post("/api/claim/submit/OP1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Proof must not be empty")));
    }
    
    @Test
    void testSubmitClaim_BusinessLogicError() throws Exception {
        String errorMessage = "Cannot submit claim, ordered plan has expired.";
        when(claimService.createClaim(eq("OP1"), any(CreateClaimRequestDTO.class)))
                .thenThrow(new IllegalStateException(errorMessage));

        mockMvc.perform(post("/api/claim/submit/OP1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createClaimDTO)))
                .andExpect(status().isInternalServerError()) 
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    void testProcessClaim_Success() throws Exception {
        claimDetailDTO.setStatus("ACCEPTED"); 
        when(claimService.processClaim(eq("CLM1"), any(ProcessClaimRequestDTO.class))).thenReturn(claimDetailDTO);

        mockMvc.perform(put("/api/claim/process/CLM1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processClaimDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Claim processed successfully."))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"));
    }

    @Test
    void testProcessClaim_ValidationError() throws Exception {
        ProcessClaimRequestDTO invalidDTO = new ProcessClaimRequestDTO();
        invalidDTO.setAcceptedNote("Note");

        mockMvc.perform(put("/api/claim/process/CLM1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Decision (isAccepted) must not be empty")));
    }
    
    @Test
    void testProcessClaim_BusinessLogicError() throws Exception {
        String errorMessage = "Claim is not waiting for review.";
   
        when(claimService.processClaim(eq("CLM1"), any(ProcessClaimRequestDTO.class)))
                .thenThrow(new IllegalStateException(errorMessage));

        mockMvc.perform(put("/api/claim/process/CLM1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processClaimDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Failed to process claim: " + errorMessage));
    }
}