package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.InsurancePlanService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsurancePlanRestController.class)
@AutoConfigureMockMvc(addFilters = false) // Matikan Security Filter
class InsurancePlanRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private InsurancePlanService insurancePlanService;
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
    void testCreatePlan_Success() throws Exception {
        setupMockUser("prov-1", "ROLE_INSURANCE_PROVIDER");
        
        CreateInsurancePlanRequestDTO req = new CreateInsurancePlanRequestDTO();
        // === [PENTING] ISI FIELD DUMMY BIAR GAK ERROR 400 ===
        // Sesuaikan dengan nama variabel di DTO kamu!
        // req.setName("Plan Sehat");
        // req.setMonthlyPremium(50000.0);
        // req.setCoverageAmount(1000000.0);

        when(insurancePlanService.createInsurancePlan(any())).thenReturn(new InsurancePlanResponseDTO());

        mockMvc.perform(post("/api/insurance-plan/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdatePlan_Success() throws Exception {
        setupMockUser("prov-1", "ROLE_INSURANCE_PROVIDER");

        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        req.setId("plan-1");
        // === [PENTING] ISI FIELD LAIN JIKA ADA VALIDASI @NotNull ===

        InsurancePlanResponseDTO existingPlan = new InsurancePlanResponseDTO();
        existingPlan.setProviderId("prov-1"); // Milik user sendiri
        
        when(insurancePlanService.getPlanById("plan-1")).thenReturn(existingPlan);
        when(insurancePlanService.updateInsurancePlan(any())).thenReturn(existingPlan);

        mockMvc.perform(put("/api/insurance-plan/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePlan_Forbidden() throws Exception {
        setupMockUser("prov-maling", "ROLE_INSURANCE_PROVIDER");

        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        req.setId("plan-1");

        InsurancePlanResponseDTO existingPlan = new InsurancePlanResponseDTO();
        existingPlan.setProviderId("prov-asli"); // Punya orang lain

        when(insurancePlanService.getPlanById("plan-1")).thenReturn(existingPlan);

        mockMvc.perform(put("/api/insurance-plan/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    void testDeletePlan_Success() throws Exception {
        setupMockUser("prov-1", "ROLE_INSURANCE_PROVIDER");
        
        InsurancePlanResponseDTO existingPlan = new InsurancePlanResponseDTO();
        existingPlan.setProviderId("prov-1");

        when(insurancePlanService.getPlanById("plan-1")).thenReturn(existingPlan);
        when(insurancePlanService.softDeletePlan("plan-1")).thenReturn(existingPlan);

        mockMvc.perform(delete("/api/insurance-plan/delete/plan-1"))
                .andExpect(status().isOk());
    }
    
    @Test
    void testGetAllPlans() throws Exception {
        setupMockUser("user", "ROLE_CUSTOMER");
        when(insurancePlanService.getAllPlans()).thenReturn(Collections.emptyList());
        
        mockMvc.perform(get("/api/insurance-plan"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPlanById() throws Exception {
        setupMockUser("user", "ROLE_CUSTOMER");
        when(insurancePlanService.getPlanById("p1")).thenReturn(new InsurancePlanResponseDTO());

        mockMvc.perform(get("/api/insurance-plan/p1"))
                .andExpect(status().isOk());
    }
}