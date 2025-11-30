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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClaimRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ClaimService claimService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private SecurityContext securityContext;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private void setupMockUser(String userId, String role) {
        when(authentication.getPrincipal()).thenReturn(userId);
        when(authentication.getAuthorities()).thenReturn((List) Collections.singletonList(new SimpleGrantedAuthority(role)));
    }

    @Test
    void testSubmitClaim_Success() throws Exception {
        setupMockUser("cust-1", "ROLE_CUSTOMER");

        CreateClaimRequestDTO req = new CreateClaimRequestDTO();
        // === [PENTING] ISI SEMUA FIELD WAJIB ===
        // req.setClaimAmount(500000.0);
        // req.setDescription("Sakit Demam");
        // req.setBankName("BCA");
        // req.setAccountNumber("1234567890");

        when(claimService.createClaim(anyString(), any())).thenReturn(new ClaimDetailResponseDTO());

        mockMvc.perform(post("/api/claim/submit/ordered-plan-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void testProcessClaim_Success() throws Exception {
        setupMockUser("admin", "ROLE_SUPERADMIN");
        
        ProcessClaimRequestDTO req = new ProcessClaimRequestDTO();
        // === [PENTING] ISI FIELD WAJIB ===
        // req.setStatus("APPROVED");

        when(claimService.processClaim(anyString(), any())).thenReturn(new ClaimDetailResponseDTO());

        mockMvc.perform(put("/api/claim/process/claim-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
    
    // ... Copy test GET lainnya dari chat sebelumnya ...
    @Test
    void testGetAllClaimsFiltered_Success() throws Exception {
        setupMockUser("admin", "ROLE_SUPERADMIN");
        when(claimService.getAllClaimsFiltered(any(), any())).thenReturn(List.of(new ClaimSummaryResponseDTO()));
        mockMvc.perform(get("/api/claim")).andExpect(status().isOk());
    }

    @Test
    void testGetClaimById_Success_Owner() throws Exception {
        String userId = "cust-1";
        setupMockUser(userId, "ROLE_CUSTOMER");
        when(claimService.getClaimById("c1")).thenReturn(new ClaimDetailResponseDTO());
        when(claimService.isClaimOwner("c1", userId)).thenReturn(true);
        mockMvc.perform(get("/api/claim/c1")).andExpect(status().isOk());
    }
}