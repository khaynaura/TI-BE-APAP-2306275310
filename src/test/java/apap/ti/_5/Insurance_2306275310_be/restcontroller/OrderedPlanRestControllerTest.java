package apap.ti._5.Insurance_2306275310_be.restcontroller;

import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restservice.OrderedPlanService;

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

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderedPlanRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderedPlanService orderedPlanService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderedPlanDetailResponseDTO orderedPlanDTO;

    @BeforeEach
    void setUp() {
        orderedPlanDTO = OrderedPlanDetailResponseDTO.builder()
                .id("OP1")
                .insurancePlanId("INS1")
                .status("PAID")
                .expiredDate(LocalDate.now().plusDays(10))
                .claims(new ArrayList<>())
                .build();
    }

    @Test
    void testGetOrderedPlanDetail_Success() throws Exception {
        when(orderedPlanService.getOrderedPlanDetailById("OP1")).thenReturn(orderedPlanDTO);

        mockMvc.perform(get("/api/ordered-plan/OP1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Ordered Plan detail retrieved successfully."))
                .andExpect(jsonPath("$.data.id").value("OP1"))
                .andExpect(jsonPath("$.data.status").value("PAID"));
    }

    @Test
    void testGetOrderedPlanDetail_NotFound() throws Exception {
        String errorMessage = "Ordered Plan not found";
        when(orderedPlanService.getOrderedPlanDetailById("OP99")).thenThrow(new RuntimeException(errorMessage));

        mockMvc.perform(get("/api/ordered-plan/OP99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }
}