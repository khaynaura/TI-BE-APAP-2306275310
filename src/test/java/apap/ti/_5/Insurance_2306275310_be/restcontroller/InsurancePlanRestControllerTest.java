package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.InsurancePlanService;
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

@SpringBootTest(properties = {
    "profile.service.url=http://localhost:8081/api",
    "insurance.api-key=test"
})
@AutoConfigureMockMvc
public class InsurancePlanRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private InsurancePlanService insurancePlanService;
    @Autowired private ObjectMapper objectMapper;

    private InsurancePlanResponseDTO planDTO;
    private CreateInsurancePlanRequestDTO createDTO;
    private UpdateInsurancePlanRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        planDTO = InsurancePlanResponseDTO.builder().id("INS1").providerId("PROV1").planName("Plan A").build();
        createDTO = CreateInsurancePlanRequestDTO.builder()
                .planName("New").providerId("PROV1").price(100).coverage(100).expiredByDays(30)
                .coverageDetails("Det").applicableService(List.of(ServiceEnum.FLIGHT)).build();
        updateDTO = UpdateInsurancePlanRequestDTO.builder()
                .id("INS1").planName("Up").price(100).coverage(100).expiredByDays(30)
                .coverageDetails("Det").applicableService(List.of(ServiceEnum.FLIGHT)).build();
    }

    // --- GET ALL ---
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllPlans_Success() throws Exception {
        when(insurancePlanService.getAllPlans()).thenReturn(List.of(planDTO));
        mockMvc.perform(get("/api/insurance-plan")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllPlans_WithSearch() throws Exception {
        when(insurancePlanService.searchPlansByName("Plan")).thenReturn(List.of(planDTO));
        mockMvc.perform(get("/api/insurance-plan").param("search", "Plan")).andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetAllPlans_Error() throws Exception {
        when(insurancePlanService.getAllPlans()).thenThrow(new RuntimeException("DB Error"));
        mockMvc.perform(get("/api/insurance-plan")).andExpect(status().isInternalServerError());
    }

    // --- GET BY PROVIDER ---
    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetByProvider_Superadmin_Success() throws Exception {
        when(insurancePlanService.getPlansByProviderId("PROV1")).thenReturn(List.of(planDTO));
        mockMvc.perform(get("/api/insurance-plan/by-provider").param("providerId", "PROV1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetByProvider_Superadmin_MissingParam() throws Exception {
        mockMvc.perform(get("/api/insurance-plan/by-provider")) // No param
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "PROV1", roles = "INSURANCE_PROVIDER")
    void testGetByProvider_Provider_Success() throws Exception {
        when(insurancePlanService.getPlansByProviderId("PROV1")).thenReturn(List.of(planDTO));
        mockMvc.perform(get("/api/insurance-plan/by-provider")) // Auto use PROV1
                .andExpect(status().isOk());
    }

    // --- GET DETAIL ---
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDetail_Success() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        mockMvc.perform(get("/api/insurance-plan/INS1")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetDetail_NotFound() throws Exception {
        when(insurancePlanService.getPlanById("INS99")).thenReturn(null);
        mockMvc.perform(get("/api/insurance-plan/INS99")).andExpect(status().isNotFound());
    }

    // --- CREATE ---
    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testCreate_Admin_Success() throws Exception {
        when(insurancePlanService.createInsurancePlan(any())).thenReturn(planDTO);
        mockMvc.perform(post("/api/insurance-plan/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testCreate_Admin_MissingProviderId() throws Exception {
        createDTO.setProviderId(null); // Kosongin
        mockMvc.perform(post("/api/insurance-plan/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testCreate_ValidationError() throws Exception {
        CreateInsurancePlanRequestDTO empty = new CreateInsurancePlanRequestDTO();
        mockMvc.perform(post("/api/insurance-plan/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(empty)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testCreate_Error() throws Exception {
        when(insurancePlanService.createInsurancePlan(any())).thenThrow(new RuntimeException("Error"));
        mockMvc.perform(post("/api/insurance-plan/create").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isInternalServerError());
    }

    // --- UPDATE ---
    @Test
    @WithMockUser(username = "PROV1", roles = "INSURANCE_PROVIDER")
    void testUpdate_Provider_OwnData_Success() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO); // Owner match
        when(insurancePlanService.updateInsurancePlan(any())).thenReturn(planDTO);

        mockMvc.perform(put("/api/insurance-plan/update").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "MALING", roles = "INSURANCE_PROVIDER")
    void testUpdate_Provider_OtherData_Forbidden() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO); // Owner PROV1

        mockMvc.perform(put("/api/insurance-plan/update").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testUpdate_NotFound() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(null);
        mockMvc.perform(put("/api/insurance-plan/update").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
    }

    // --- DELETE ---
    @Test
    @WithMockUser(username = "PROV1", roles = "INSURANCE_PROVIDER")
    void testDelete_Provider_Success() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        when(insurancePlanService.softDeletePlan("INS1")).thenReturn(planDTO);
        mockMvc.perform(delete("/api/insurance-plan/delete/INS1").with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "MALING", roles = "INSURANCE_PROVIDER")
    void testDelete_Provider_Forbidden() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        mockMvc.perform(delete("/api/insurance-plan/delete/INS1").with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testDelete_LogicError() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        when(insurancePlanService.softDeletePlan("INS1")).thenThrow(new IllegalStateException("Active Orders"));
        mockMvc.perform(delete("/api/insurance-plan/delete/INS1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetByService_Success() throws Exception {
        when(insurancePlanService.getPlansByApplicableService(ServiceEnum.FLIGHT)).thenReturn(List.of(planDTO));
        mockMvc.perform(get("/api/insurance-plan/by-service").param("service", "FLIGHT"))
                .andExpect(status().isOk());
    }

    // 1. Test Exception di GET BY PROVIDER (Menghijaukan blok Catch)
    @Test
    @WithMockUser(username = "PROV1", roles = "INSURANCE_PROVIDER")
    void testGetByProvider_Exception_InternalServerError() throws Exception {
        // Simulasi DB Error saat fetch provider
        when(insurancePlanService.getPlansByProviderId(any())).thenThrow(new RuntimeException("DB Error"));
        
        mockMvc.perform(get("/api/insurance-plan/by-provider"))
                .andExpect(status().isInternalServerError()); // Harus 500
    }

    // 2. Test Exception di UPDATE (Menghijaukan blok Catch)
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testUpdate_Exception_InternalServerError() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        // Simulasi DB Error saat save update
        when(insurancePlanService.updateInsurancePlan(any())).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(put("/api/insurance-plan/update").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isInternalServerError()); // Harus 500
    }

    // 3. Test Exception di DELETE (Menghijaukan blok Catch)
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testDelete_Exception_InternalServerError() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);
        // Simulasi DB Error saat delete
        when(insurancePlanService.softDeletePlan("INS1")).thenThrow(new RuntimeException("DB Error"));

        mockMvc.perform(delete("/api/insurance-plan/delete/INS1").with(csrf()))
                .andExpect(status().isInternalServerError()); // Harus 500
    }
    
    // 4. Test Exception di GET BY SERVICE (Menghijaukan blok Catch)
    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetByService_Exception_InternalServerError() throws Exception {
        when(insurancePlanService.getPlansByApplicableService(any())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/insurance-plan/by-service").param("service", "FLIGHT"))
                .andExpect(status().isInternalServerError());
    }
}