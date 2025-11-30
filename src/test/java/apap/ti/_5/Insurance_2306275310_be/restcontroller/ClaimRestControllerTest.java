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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "profile.service.url=http://localhost:8081/api" })
@AutoConfigureMockMvc
public class ClaimRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ClaimService claimService;
    @Autowired private ObjectMapper objectMapper;

    private ClaimSummaryResponseDTO summaryDTO;
    private ClaimDetailResponseDTO detailDTO;
    private CreateClaimRequestDTO createDTO;
    private ProcessClaimRequestDTO processDTO;

    @BeforeEach
    void setUp() {
        summaryDTO = ClaimSummaryResponseDTO.builder().id("CLM1").build();
        detailDTO = ClaimDetailResponseDTO.builder().id("CLM1").build();
        createDTO = new CreateClaimRequestDTO("Proof");
        processDTO = ProcessClaimRequestDTO.builder().isAccepted(true).build();
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetAll_Success() throws Exception {
        when(claimService.getAllClaimsFiltered(any(), any())).thenReturn(List.of(summaryDTO));
        mockMvc.perform(get("/api/claim")).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetAll_Error() throws Exception {
        when(claimService.getAllClaimsFiltered(any(), any())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(get("/api/claim")).andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetDetail_Success() throws Exception {
        when(claimService.getClaimById("CLM1")).thenReturn(detailDTO);
        when(claimService.isClaimOwner("CLM1", "budi")).thenReturn(true);
        mockMvc.perform(get("/api/claim/CLM1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetDetail_Forbidden() throws Exception {
        when(claimService.getClaimById("CLM1")).thenReturn(detailDTO);
        when(claimService.isClaimOwner("CLM1", "budi")).thenReturn(false); // Not owner
        mockMvc.perform(get("/api/claim/CLM1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetDetail_NotFound() throws Exception {
        when(claimService.getClaimById("CLM99")).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/claim/CLM99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmit_Success() throws Exception {
        when(claimService.createClaim(eq("OP1"), any())).thenReturn(detailDTO);
        mockMvc.perform(post("/api/claim/submit/OP1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmit_ValidationError() throws Exception {
        CreateClaimRequestDTO invalid = new CreateClaimRequestDTO(""); // Proof empty
        mockMvc.perform(post("/api/claim/submit/OP1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "INSURANCE_PROVIDER")
    void testProcess_Success() throws Exception {
        when(claimService.processClaim(eq("CLM1"), any())).thenReturn(detailDTO);
        mockMvc.perform(put("/api/claim/process/CLM1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processDTO)))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "INSURANCE_PROVIDER")
    void testProcess_Error() throws Exception {
        when(claimService.processClaim(any(), any())).thenThrow(new RuntimeException("Fail"));
        mockMvc.perform(put("/api/claim/process/CLM1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processDTO)))
                .andExpect(status().isInternalServerError());
    }

    // 1. Test Create Claim Validation Error (Menghijaukan blok bindingResult.hasErrors)
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmitClaim_ValidationError() throws Exception {
        // Buat DTO yang tidak valid (misal proof kosong kalau ada validasi @NotBlank)
        CreateClaimRequestDTO invalidDto = new CreateClaimRequestDTO(""); 
        
        mockMvc.perform(post("/api/claim/submit/OP1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest()); // Harus 400 Bad Request
    }

    // 2. Test Create Claim Server Error (Menghijaukan blok catch Exception)
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testSubmitClaim_ServerError() throws Exception {
        // Simulasi service error
        when(claimService.createClaim(any(), any())).thenThrow(new RuntimeException("DB Down"));
        
        mockMvc.perform(post("/api/claim/submit/OP1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO))) // Pakai DTO valid
                .andExpect(status().isInternalServerError()); // Harus 500 Internal Server Error
    }

    // 3. Test Process Claim Validation Error (Menghijaukan blok bindingResult.hasErrors)
    @Test
    @WithMockUser(roles = "INSURANCE_PROVIDER")
    void testProcessClaim_ValidationError() throws Exception {
        // DTO Invalid (misal null)
        ProcessClaimRequestDTO invalidProcessDto = new ProcessClaimRequestDTO(); 
        
        mockMvc.perform(put("/api/claim/process/CLM1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProcessDto)))
                .andExpect(status().isBadRequest());
    }

    // 4. Test Process Claim Server Error (Menghijaukan blok catch Exception)
    @Test
    @WithMockUser(roles = "INSURANCE_PROVIDER")
    void testProcessClaim_ServerError() throws Exception {
        when(claimService.processClaim(any(), any())).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(put("/api/claim/process/CLM1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(processDTO)))
                .andExpect(status().isInternalServerError());
    }
}