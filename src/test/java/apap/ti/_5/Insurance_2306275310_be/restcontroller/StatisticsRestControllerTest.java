package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.StatisticsService;

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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class StatisticsRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    private HomeSummaryResponseDTO summaryDTO;
    private ChartDataResponseDTO chartDTO;

    @BeforeEach
    void setUp() {
        summaryDTO = HomeSummaryResponseDTO.builder()
                .totalInsurancePlans(10L)
                .totalPolicies(50L)
                .totalClaimsProcessed(100L)
                .build();
        
        chartDTO = ChartDataResponseDTO.builder()
                .labels(List.of("Jan", "Feb", "Mar"))
                .data(List.of(5L, 10L, 15L))
                .build();
    }

    @Test
    void testGetHomeSummary_Success() throws Exception {
        when(statisticsService.getHomeSummary()).thenReturn(summaryDTO);

        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Ringkasan data berhasil diambil"))
                .andExpect(jsonPath("$.data.totalPolicies").value(50L));
    }

    @Test
    void testGetHomeSummary_ServerError() throws Exception {
        when(statisticsService.getHomeSummary()).thenThrow(new RuntimeException("DB Connection Error"));

        mockMvc.perform(get("/api/statistics/summary"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan pada server: DB Connection Error"));
    }

    @Test
    void testGetChartStatistics_Success() throws Exception {
        when(statisticsService.getChartStatistics(anyInt(), anyString())).thenReturn(chartDTO);

        mockMvc.perform(get("/api/statistics/chart")
                .param("period", "3")
                .param("service", "FLIGHT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Chart data retrieved successfully."))
                .andExpect(jsonPath("$.data.labels", hasSize(3)))
                .andExpect(jsonPath("$.data.data[1]").value(10L));
        
        // Verify service was called with correct params
        verify(statisticsService, times(1)).getChartStatistics(3, "FLIGHT");
    }

    @Test
    void testGetChartStatistics_ServerError() throws Exception {
        when(statisticsService.getChartStatistics(anyInt(), anyString()))
                .thenThrow(new RuntimeException("Query Error"));

        mockMvc.perform(get("/api/statistics/chart")
                .param("period", "3")
                .param("service", "FLIGHT"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Terjadi kesalahan pada server: Query Error"));
    }
}