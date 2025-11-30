package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.StatisticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
public class StatisticsRestControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private StatisticsService statisticsService;

    @Test
    @WithMockUser(username = "admin", roles = "SUPERADMIN")
    void testGetChart_AsAdmin() throws Exception {
        ChartDataResponseDTO dto = new ChartDataResponseDTO(List.of("Jan"), List.of(10L));
        when(statisticsService.getChartStatistics(eq(3), eq("FLIGHT"), eq(null))).thenReturn(dto);

        mockMvc.perform(get("/api/statistics/chart?period=3&service=FLIGHT"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "provider1", roles = "INSURANCE_PROVIDER")
    void testGetChart_AsProvider() throws Exception {
        ChartDataResponseDTO dto = new ChartDataResponseDTO(List.of("Jan"), List.of(5L));
        // providerId "provider1" karena username mock = provider1
        when(statisticsService.getChartStatistics(eq(3), eq("FLIGHT"), eq("provider1"))).thenReturn(dto);

        mockMvc.perform(get("/api/statistics/chart?period=3&service=FLIGHT"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "budi", roles = "CUSTOMER")
    void testGetSummary_AsCustomer() throws Exception {
        when(statisticsService.getHomeSummary(anyString(), anyString())).thenReturn(new HomeSummaryResponseDTO());
        
        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isOk());
    }

     @Test
    @WithMockUser(roles = "SUPERADMIN")
    void testGetChart_Exception_InternalServerError() throws Exception {
        // Simulasi Service Error (Database mati, logic error, dll)
        when(statisticsService.getChartStatistics(anyInt(), anyString(), any()))
                .thenThrow(new RuntimeException("Database Connection Failed"));

        mockMvc.perform(get("/api/statistics/chart?period=3&service=FLIGHT"))
                .andExpect(status().isInternalServerError()); // Harapan: 500
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void testGetSummary_Exception_InternalServerError() throws Exception {
        // Simulasi Service Error
        when(statisticsService.getHomeSummary(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service Error"));

        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isInternalServerError()); // Harapan: 500
    }
}