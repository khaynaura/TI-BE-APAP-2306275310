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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class InsurancePlanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsurancePlanService insurancePlanService;

    @Autowired
    private ObjectMapper objectMapper;

    private InsurancePlanResponseDTO planDTO;
    private CreateInsurancePlanRequestDTO createDTO;
    private UpdateInsurancePlanRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        planDTO = InsurancePlanResponseDTO.builder()
                .id("INS1")
                .planName("Test Plan")
                .price(100)
                .coverage(1000)
                .applicableService(List.of(ServiceEnum.FLIGHT))
                .expiredByDays(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createDTO = CreateInsurancePlanRequestDTO.builder()
                .planName("New Plan")
                .providerId("P-1")
                .price(200)
                .coverage(2000)
                .coverageDetails("Details")
                .applicableService(List.of(ServiceEnum.FLIGHT))
                .expiredByDays(45)
                .build();
        
        updateDTO = UpdateInsurancePlanRequestDTO.builder()
                .id("INS1")
                .planName("Updated Plan")
                .price(110)
                .coverage(1100)
                .coverageDetails("Updated Details")
                .applicableService(List.of(ServiceEnum.ACCOMMODATION))
                .expiredByDays(35)
                .build();
    }

    @Test
    void testGetAllPlans_NoSearch() throws Exception {
        when(insurancePlanService.getAllPlans()).thenReturn(List.of(planDTO));

        mockMvc.perform(get("/api/insurance-plan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Semua Insurance Plan berhasil diambil"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].id").value("INS1"));
    }

    @Test
    void testGetAllPlans_WithSearch() throws Exception {
        when(insurancePlanService.searchPlansByName("Test")).thenReturn(List.of(planDTO));

        mockMvc.perform(get("/api/insurance-plan").param("search", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Hasil pencarian Insurance Plan berhasil diambil"))
                .andExpect(jsonPath("$.data[0].planName").value("Test Plan"));
    }

    @Test
    void testGetPlanById_Success() throws Exception {
        when(insurancePlanService.getPlanById("INS1")).thenReturn(planDTO);

        mockMvc.perform(get("/api/insurance-plan/INS1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("INS1"));
    }

    @Test
    void testGetPlanById_NotFound() throws Exception {
        when(insurancePlanService.getPlanById("INS99")).thenReturn(null); // Service returns null

        mockMvc.perform(get("/api/insurance-plan/INS99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Insurance Plan dengan ID INS99 tidak ditemukan"));
    }

    @Test
    void testCreateInsurancePlan_Success() throws Exception {
        when(insurancePlanService.createInsurancePlan(any(CreateInsurancePlanRequestDTO.class))).thenReturn(planDTO);

        mockMvc.perform(post("/api/insurance-plan/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value("INS1"));
    }

    @Test
    void testCreateInsurancePlan_ValidationError() throws Exception {
        CreateInsurancePlanRequestDTO invalidDTO = CreateInsurancePlanRequestDTO.builder()
                .providerId("P-1").price(100).coverage(1000).expiredByDays(10).build();

        mockMvc.perform(post("/api/insurance-plan/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Plan name must not be empty")));
    }
    
    @Test
    void testUpdateInsurancePlan_Success() throws Exception {
        when(insurancePlanService.updateInsurancePlan(any(UpdateInsurancePlanRequestDTO.class))).thenReturn(planDTO);

        mockMvc.perform(put("/api/insurance-plan/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value("INS1"));
    }
    
    @Test
    void testUpdateInsurancePlan_ValidationError() throws Exception {
        UpdateInsurancePlanRequestDTO invalidDTO = UpdateInsurancePlanRequestDTO.builder()
                .id("INS1").planName("Name").build();
        
        mockMvc.perform(put("/api/insurance-plan/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("Price must not be null")));
    }
    
    @Test
    void testUpdateInsurancePlan_NotFound() throws Exception {
        when(insurancePlanService.updateInsurancePlan(any(UpdateInsurancePlanRequestDTO.class))).thenReturn(null);
        
        mockMvc.perform(put("/api/insurance-plan/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Insurance Plan dengan ID INS1 tidak ditemukan"));
    }
    
    @Test
    void testDeleteInsurancePlan_Success() throws Exception {
        when(insurancePlanService.softDeletePlan("INS1")).thenReturn(planDTO);

        mockMvc.perform(delete("/api/insurance-plan/delete/INS1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Data Insurance Plan berhasil dihapus"))
                .andExpect(jsonPath("$.data.id").value("INS1"));
    }

    @Test
    void testDeleteInsurancePlan_NotFound() throws Exception {
        when(insurancePlanService.softDeletePlan("INS99")).thenReturn(null);

        mockMvc.perform(delete("/api/insurance-plan/delete/INS99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Insurance Plan dengan ID INS99 tidak ditemukan"));
    }

    @Test
    void testDeleteInsurancePlan_BusinessLogicError() throws Exception {
        String errorMessage = "Plan tidak dapat dihapus karena satu atau lebih Ordered Plan terkait belum expired.";
        when(insurancePlanService.softDeletePlan("INS1")).thenThrow(new IllegalStateException(errorMessage));

        mockMvc.perform(delete("/api/insurance-plan/delete/INS1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Gagal menghapus: " + errorMessage));
    }
    
    @Test
    void testGetPlansByService_Success() throws Exception {
        when(insurancePlanService.getPlansByApplicableService(ServiceEnum.FLIGHT)).thenReturn(List.of(planDTO));

        mockMvc.perform(get("/api/insurance-plan/by-service").param("service", "FLIGHT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }
    
    @Test
    void testGetPlansByService_ServerError() throws Exception {
        when(insurancePlanService.getPlansByApplicableService(ServiceEnum.FLIGHT)).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/insurance-plan/by-service").param("service", "FLIGHT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan pada server: Database error"));
    }
}