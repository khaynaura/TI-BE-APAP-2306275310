package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.OrderedPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "profile.service.url=http://localhost:8081/api",
    "billing.service.url=http://localhost:8085/api",
    "accommodation.service.url=http://localhost:8087/api",
    "flight.service.url=http://localhost:8086/api",
    "rental.service.url=http://localhost:8088/api",
    "package.service.url=http://localhost:8089/api",
    "insurance.api-key=test-key",
    "frontend.url=http://localhost:5173"
})
@AutoConfigureMockMvc
public class OrderedPlanRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private OrderedPlanService orderedPlanService;

    private OrderedPlanDetailResponseDTO detailDTO;

    @BeforeEach
    void setUp() {
        // [PENTING] Kita set pemilik data ini adalah "budi"
        detailDTO = OrderedPlanDetailResponseDTO.builder()
                .id("OP1")
                .status("PAID")
                .customerId("budi") 
                .build();
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetDetail_AsOwner_Success() throws Exception {
        // Skenario: Budi login, mau liat data punya Budi
        when(orderedPlanService.getOrderedPlanDetailById("OP1")).thenReturn(detailDTO);

        mockMvc.perform(get("/api/ordered-plan/OP1"))
                .andExpect(status().isOk()) // Harusnya 200
                .andExpect(jsonPath("$.data.id").value("OP1"));
    }

    @Test
    @WithMockUser(username = "siti", roles = "CUSTOMER")
    void testGetDetail_AsOtherCustomer_Forbidden() throws Exception {
        // Skenario: Siti login, mau liat data punya Budi
        // Controller cek: "budi" (di DTO) != "siti" (yang login) -> Forbidden
        when(orderedPlanService.getOrderedPlanDetailById("OP1")).thenReturn(detailDTO);

        mockMvc.perform(get("/api/ordered-plan/OP1"))
                .andExpect(status().isForbidden()); // Harusnya 403
    }

    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetDetail_AsAdmin_Success() throws Exception {
        // Skenario: Admin login (Controller skip validasi kepemilikan buat admin)
        when(orderedPlanService.getOrderedPlanDetailById("OP1")).thenReturn(detailDTO);

        mockMvc.perform(get("/api/ordered-plan/OP1"))
                .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetDetail_NotFound() throws Exception {
        when(orderedPlanService.getOrderedPlanDetailById("OP99"))
                .thenThrow(new RuntimeException("Not Found"));

        mockMvc.perform(get("/api/ordered-plan/OP99"))
                .andExpect(status().isNotFound());
    }
}