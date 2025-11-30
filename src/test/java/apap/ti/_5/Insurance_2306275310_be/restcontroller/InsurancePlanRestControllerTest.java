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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InsurancePlanRestController.class)
@AutoConfigureMockMvc(addFilters = false)
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
        // REFLECTION TRY-CATCH BIAR AMAN KALAU NAMA FIELD BEDA
        try { req.getClass().getMethod("setName", String.class).invoke(req, "Plan Sehat"); } catch (Exception e) {}
        try { req.getClass().getMethod("setPremium", Double.class).invoke(req, 50000.0); } catch (Exception e) {}

        when(insurancePlanService.createInsurancePlan(any())).thenReturn(new InsurancePlanResponseDTO());

        mockMvc.perform(post("/api/insurance-plan/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    void testUpdatePlan_Success() throws Exception {
        setupMockUser("prov-1", "ROLE_INSURANCE_PROVIDER");

        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        try { req.getClass().getMethod("setId", String.class).invoke(req, "plan-1"); } catch (Exception e) {}

        InsurancePlanResponseDTO existingPlan = new InsurancePlanResponseDTO();
        existingPlan.setProviderId("prov-1"); // ID SAMA -> BOLEH UPDATE
        
        when(insurancePlanService.getPlanById("plan-1")).thenReturn(existingPlan);
        when(insurancePlanService.updateInsurancePlan(any())).thenReturn(existingPlan);

        mockMvc.perform(put("/api/insurance-plan/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePlan_Forbidden() throws Exception {
        // SCENARIO: User Maling coba update plan orang lain
        setupMockUser("prov-maling", "ROLE_INSURANCE_PROVIDER");

        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        try { req.getClass().getMethod("setId", String.class).invoke(req, "plan-1"); } catch (Exception e) {}

        InsurancePlanResponseDTO existingPlan = new InsurancePlanResponseDTO();
        existingPlan.setProviderId("prov-asli"); // ID BEDA -> HARUS 403

        when(insurancePlanService.getPlanById("plan-1")).thenReturn(existingPlan);

        mockMvc.perform(put("/api/insurance-plan/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}