package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.PolicyService;

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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class PolicyRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PolicyService policyService;

    @Autowired
    private ObjectMapper objectMapper;

    private PolicyResponseDTO policyDTO;
    private CreatePolicyRequestDTO createDTO;

    @BeforeEach
    void setUp() {
        policyDTO = PolicyResponseDTO.builder()
                .id("POL1")
                .bookingId("BOOK-ABC")
                .userId("user123")
                .service(ServiceEnum.FLIGHT)
                .startDate(LocalDate.now())
                .status("CREATED")
                .totalPrice(100)
                .totalCoverage(1000)
                .orderedPlans(new ArrayList<>())
                .build();

        createDTO = CreatePolicyRequestDTO.builder()
                .userId("user123")
                .bookingId("BOOK-ABC")
                .service(ServiceEnum.FLIGHT)
                .insurancePlanIds(List.of("INS1"))
                .build();
    }

    @Test
    void testCreatePolicy_Success() throws Exception {
        when(policyService.createPolicy(any(CreatePolicyRequestDTO.class))).thenReturn(policyDTO);

        mockMvc.perform(post("/api/policy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Policy created successfully."))
                .andExpect(jsonPath("$.data.id").value("POL1"));
    }

    @Test
    void testCreatePolicy_ValidationError() throws Exception {
        CreatePolicyRequestDTO invalidDTO = CreatePolicyRequestDTO.builder()
                .bookingId("BOOK-ABC")
                .service(ServiceEnum.FLIGHT)
                .insurancePlanIds(List.of("INS1"))
                .build();

        mockMvc.perform(post("/api/policy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("User ID must not be empty")));
    }
    
    @Test
    void testCreatePolicy_ServerError() throws Exception {
        when(policyService.createPolicy(any(CreatePolicyRequestDTO.class)))
                .thenThrow(new RuntimeException("One or more Insurance Plan IDs are invalid."));

        mockMvc.perform(post("/api/policy/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Server error: One or more Insurance Plan IDs are invalid."));
    }

    @Test
    void testGetAllPolicies_Success() throws Exception {
        when(policyService.getAllPolicies()).thenReturn(List.of(policyDTO));

        mockMvc.perform(get("/api/policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("All policies retrieved successfully."))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value("POL1"));
    }
    
    @Test
    void testGetPolicyById_Success() throws Exception {
        when(policyService.getPolicyById("POL1")).thenReturn(policyDTO);

        mockMvc.perform(get("/api/policy/POL1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Policy detail retrieved successfully."))
                .andExpect(jsonPath("$.data.id").value("POL1"));
    }

    @Test
    void testGetPolicyById_NotFound() throws Exception {
        when(policyService.getPolicyById("POL99")).thenThrow(new RuntimeException("Policy not found"));

        mockMvc.perform(get("/api/policy/POL99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Policy not found"));
    }
    
    @Test
    void testPayPolicy_Success() throws Exception {
        policyDTO.setStatus("PAID");
        when(policyService.payPolicy("POL1")).thenReturn(policyDTO);

        mockMvc.perform(put("/api/policy/pay/POL1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Payment successful."))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void testPayPolicy_BusinessLogicError() throws Exception {
        String errorMessage = "Policy cannot be paid. Status is: PAID";
        when(policyService.payPolicy("POL1")).thenThrow(new IllegalStateException(errorMessage));

        mockMvc.perform(put("/api/policy/pay/POL1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
    
    @Test
    void testPayPolicy_NotFound() throws Exception {
        String errorMessage = "Policy not found";
        when(policyService.payPolicy("POL99")).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(put("/api/policy/pay/POL99"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Server error: " + errorMessage));
    }
}