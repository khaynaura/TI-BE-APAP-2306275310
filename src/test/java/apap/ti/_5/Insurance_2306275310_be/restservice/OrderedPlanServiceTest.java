package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderedPlanServiceTest {

    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @InjectMocks
    private OrderedPlanServiceImpl orderedPlanService;

    private OrderedPlan orderedPlan;
    private InsurancePlan insurancePlan;
    private Claim claim;

    @BeforeEach
    void setUp() {
        insurancePlan = InsurancePlan.builder()
                .id("INS1")
                .planName("Flight Plan")
                .build();

        orderedPlan = OrderedPlan.builder()
                .id("OP1")
                .status("PAID")
                .expiredDate(LocalDate.now().plusDays(30))
                .insurancePlan(insurancePlan)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now())
                .claims(new ArrayList<>())
                .build();
        
        claim = Claim.builder()
                .id("OP1-CLAIM1")
                .status("WAITING_FOR_REVIEW")
                .orderedPlan(orderedPlan)
                .createdAt(LocalDateTime.now())
                .build();
        
        orderedPlan.setClaims(List.of(claim));
    }

    @Test
    void testGetOrderedPlanDetailById_Success() {
        when(orderedPlanRepository.findById("OP1")).thenReturn(Optional.of(orderedPlan));

        OrderedPlanDetailResponseDTO result = orderedPlanService.getOrderedPlanDetailById("OP1");

        assertNotNull(result);
        assertEquals("OP1", result.getId());
        assertEquals("INS1", result.getInsurancePlanId());
        assertEquals("PAID", result.getStatus());

        assertNotNull(result.getClaims());
        assertEquals(1, result.getClaims().size());
        assertEquals("OP1-CLAIM1", result.getClaims().get(0).getId());
        assertEquals("Flight Plan", result.getClaims().get(0).getPlanName());

        assertNotNull(result.getClaims().get(0).getDaysSinceClaimed());

        verify(orderedPlanRepository, times(1)).findById("OP1");
    }

    @Test
    void testGetOrderedPlanDetailById_NotFound() {
        when(orderedPlanRepository.findById(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            orderedPlanService.getOrderedPlanDetailById("BAD-ID");
        });

        assertEquals("Ordered Plan not found", exception.getMessage());
        verify(orderedPlanRepository, times(1)).findById("BAD-ID");
    }
}