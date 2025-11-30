package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;
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
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = { "profile.service.url=http://localhost:8081/api" })
@AutoConfigureMockMvc
public class PolicyRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PolicyService policyService;
    @Autowired private ObjectMapper objectMapper;

    private PolicyResponseDTO policyDTO;
    private CreatePolicyRequestDTO createDTO;

    @BeforeEach
    void setUp() {
        policyDTO = PolicyResponseDTO.builder().id("POL1").userId("budi").status("CREATED").build();
        createDTO = CreatePolicyRequestDTO.builder()
                .bookingId("B1").service(ServiceEnum.FLIGHT).insurancePlanIds(List.of("INS1")).userId("temp")
                .build();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreate_Success() throws Exception {
        when(policyService.createPolicy(any())).thenReturn(policyDTO);
        mockMvc.perform(post("/api/policy/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreate_ValidationError() throws Exception {
        CreatePolicyRequestDTO invalid = new CreatePolicyRequestDTO();
        mockMvc.perform(post("/api/policy/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testCreate_LogicError() throws Exception {
        when(policyService.createPolicy(any())).thenThrow(new IllegalArgumentException("Booking Invalid"));
        mockMvc.perform(post("/api/policy/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetAll_Admin() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(policyDTO));
        mockMvc.perform(get("/api/policy")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetAll_Customer() throws Exception {
        when(policyService.getPoliciesByUserId("budi")).thenReturn(List.of(policyDTO));
        mockMvc.perform(get("/api/policy")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetDetail_Success() throws Exception {
        when(policyService.getPolicyById("POL1")).thenReturn(policyDTO);
        mockMvc.perform(get("/api/policy/POL1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "siti", roles = "CUSTOMER")
    void testGetDetail_Forbidden() throws Exception {
        when(policyService.getPolicyById("POL1")).thenReturn(policyDTO); // Milik Budi
        mockMvc.perform(get("/api/policy/POL1")).andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetDetail_NotFound() throws Exception {
        when(policyService.getPolicyById("POL99")).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/api/policy/POL99")).andExpect(status().isNotFound());
    }

    @Test
    void testNotifyPayment_Success() throws Exception {
        // Endpoint Public
        Map<String, Object> payload = Map.of("refId", "POL1", "status", "PAID");
        when(policyService.payPolicy("POL1")).thenReturn(policyDTO);

        mockMvc.perform(post("/api/policy/notify-payment").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }
    
    @Test
    void testNotifyPayment_Error() throws Exception {
        // Payload salah
        Map<String, Object> payload = Map.of("refId", "POL1"); // No status
        mockMvc.perform(post("/api/policy/notify-payment").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isInternalServerError());
    }

    
}