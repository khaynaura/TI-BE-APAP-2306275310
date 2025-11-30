package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.*;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderedPlanServiceImplTest {

    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @InjectMocks
    private OrderedPlanServiceImpl orderedPlanService;

    @Test
    void testGetOrderedPlanDetailById_Success() {
        String id = "OP-1";
        OrderedPlan op = new OrderedPlan();
        op.setId(id);
        op.setStatus("PAID");
        
        InsurancePlan ip = new InsurancePlan();
        ip.setId("INS-1");
        ip.setPlanName("Health Plan");
        op.setInsurancePlan(ip);

        Policy policy = new Policy();
        policy.setUserId("user-1");
        op.setPolicy(policy);

        // Add dummy claims to test conversion logic
        List<Claim> claims = new ArrayList<>();
        Claim c1 = new Claim();
        c1.setId("C1");
        c1.setStatus("WAITING_FOR_REVIEW");
        c1.setCreatedAt(LocalDateTime.now().minusDays(2));
        c1.setOrderedPlan(op);
        claims.add(c1);
        
        Claim c2 = new Claim();
        c2.setId("C2");
        c2.setStatus("ACCEPTED");
        c2.setCreatedAt(LocalDateTime.now());
        c2.setOrderedPlan(op);
        claims.add(c2);

        op.setClaims(claims);

        when(orderedPlanRepository.findById(id)).thenReturn(Optional.of(op));

        OrderedPlanDetailResponseDTO result = orderedPlanService.getOrderedPlanDetailById(id);

        assertEquals(id, result.getId());
        assertEquals("INS-1", result.getInsurancePlanId());
        assertEquals(2, result.getClaims().size());
        
        // Verify day calculation for WAITING_FOR_REVIEW
        assertEquals(2, result.getClaims().get(0).getDaysSinceClaimed());
        // Verify day calculation for others (should be 0/default logic)
        assertEquals(0, result.getClaims().get(1).getDaysSinceClaimed());
    }

    @Test
    void testGetOrderedPlanDetailById_NotFound() {
        when(orderedPlanRepository.findById("any")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> orderedPlanService.getOrderedPlanDetailById("any"));
    }
}