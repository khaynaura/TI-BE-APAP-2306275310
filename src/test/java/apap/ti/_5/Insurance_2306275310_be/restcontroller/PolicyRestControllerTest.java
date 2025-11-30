package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PolicyService policyService;
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

    private void setupMockUser(String userId) {
        when(authentication.getPrincipal()).thenReturn(userId);
    }

    @Test
    void testCreatePolicy_Success() throws Exception {
        setupMockUser("cust-1");
        
        CreatePolicyRequestDTO req = new CreatePolicyRequestDTO();
        // === [PENTING] ISI FIELD WAJIB ===
        // req.setPlanId("plan-123");
        // req.setDurationMonths(12);

        when(policyService.createPolicy(any())).thenReturn(new PolicyResponseDTO());

        mockMvc.perform(post("/api/policy/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void testReceivePaymentNotification_Success() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("serviceReferenceId", "pol-123");
        payload.put("status", "PAID");

        // Guna any() atau anyString() biar aman
        doNothing().when(policyService).payPolicy(any());

        mockMvc.perform(post("/api/policy/notify-payment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
    
    // ... Copy test GET lainnya ...
    @Test
    void testGetPolicyById_Success_Owner() throws Exception {
        String userId = "cust-1";
        setupMockUser(userId);
        PolicyResponseDTO res = new PolicyResponseDTO();
        res.setUserId(userId);
        when(policyService.getPolicyById("p1")).thenReturn(res);
        mockMvc.perform(get("/api/policy/p1")).andExpect(status().isOk());
    }
}