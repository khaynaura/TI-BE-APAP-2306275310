package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.Policy;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private InsurancePlan plan1;
    private CreatePolicyRequestDTO createDTO;
    private Policy policy;
    private OrderedPlan op1;

    @BeforeEach
    void setUp() {
        plan1 = InsurancePlan.builder()
                .id("INS1")
                .planName("Basic Plan")
                .price(100)
                .coverage(1000)
                .expiredByDays(30)
                .build();

        createDTO = CreatePolicyRequestDTO.builder()
                .userId("user123")
                .bookingId("BOOK-ABC")
                .service(ServiceEnum.FLIGHT)
                .insurancePlanIds(List.of("INS1"))
                .build();

        policy = new Policy();
        policy.setId("POL1");
        policy.setUserId("user123");
        policy.setBookingId("BOOK-ABC");
        policy.setService(ServiceEnum.FLIGHT);
        policy.setStartDate(LocalDate.now());
        policy.setStatus("CREATED");
        policy.setTotalPrice(100);
        policy.setTotalCoverage(1000);

        op1 = new OrderedPlan();
        op1.setId("POL1-OP1");
        op1.setStatus("ORDERED");
        op1.setExpiredDate(LocalDate.now().plusDays(30));
        op1.setInsurancePlan(plan1);
        op1.setPolicy(policy);

        policy.setOrderedPlans(new ArrayList<>(List.of(op1))); 
    }

    @Test
    void testCreatePolicy_Success() {
        when(insurancePlanRepository.findAllById(List.of("INS1"))).thenReturn(List.of(plan1));
        when(policyRepository.count()).thenReturn(0L); 
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);
        when(orderedPlanRepository.saveAll(anyList())).thenReturn(List.of(op1));

        PolicyResponseDTO result = policyService.createPolicy(createDTO);

        assertNotNull(result);
        assertEquals("POL1", result.getId());
        assertEquals(100, result.getTotalPrice());
        assertEquals(1000, result.getTotalCoverage());
        assertEquals(1, result.getOrderedPlans().size());
        assertEquals("POL1-OP1", result.getOrderedPlans().get(0).getId());

        verify(insurancePlanRepository, times(1)).findAllById(anyList());
        verify(policyRepository, times(1)).save(any(Policy.class));
        verify(orderedPlanRepository, times(1)).saveAll(anyList());
    }

    @Test
    void testCreatePolicy_Fail_InvalidPlanId() {

        when(insurancePlanRepository.findAllById(List.of("INS1"))).thenReturn(new ArrayList<>());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            policyService.createPolicy(createDTO);
        });

        assertEquals("One or more Insurance Plan IDs are invalid.", exception.getMessage());
        verify(policyRepository, never()).save(any());
        verify(orderedPlanRepository, never()).saveAll(anyList());
    }

    @Test
    void testPayPolicy_Success() {

        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class))).thenReturn(policy);

        PolicyResponseDTO result = policyService.payPolicy("POL1");

        assertNotNull(result);
        assertEquals("PAID", result.getStatus());

        assertEquals("PAID", policy.getStatus());
        assertEquals("PAID", op1.getStatus());
        verify(policyRepository, times(1)).save(policy);
        verify(orderedPlanRepository, times(1)).saveAll(policy.getOrderedPlans());
    }

    @Test
    void testPayPolicy_Fail_AlreadyPaid() {
        policy.setStatus("PAID");
        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            policyService.payPolicy("POL1");
        });

        assertEquals("Policy cannot be paid. Status is: PAID", exception.getMessage());
        verify(policyRepository, never()).save(any());
        verify(orderedPlanRepository, never()).saveAll(anyList());
    }

    @Test
    void testPayPolicy_Fail_NotFound() {

        when(policyRepository.findById("POL99")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            policyService.payPolicy("POL99");
        });

        assertEquals("Policy not found", exception.getMessage());
    }

    @Test
    void testGetPolicyById_Success() {
        policy.setStatus("PAID"); 
        op1.setStatus("PAID");
        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));

        PolicyResponseDTO result = policyService.getPolicyById("POL1");

        assertNotNull(result);
        assertEquals("POL1", result.getId());

        assertEquals("PAID", result.getStatus());
        verify(orderedPlanRepository, never()).save(any());
        verify(policyRepository, never()).save(any());
    }

    @Test
    void testGetPolicyById_NotFound() {
        when(policyRepository.findById("POL99")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            policyService.getPolicyById("POL99");
        });
        assertEquals("Policy not found", exception.getMessage());
    }

    @Test
    void testCheckAndSetExpiration_AllPlansExpired() {

        policy.setStatus("PAID");
        op1.setStatus("PAID");
        op1.setExpiredDate(LocalDate.now().minusDays(1)); 
        
        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));

        PolicyResponseDTO result = policyService.getPolicyById("POL1"); 
        assertEquals("EXPIRED", op1.getStatus());
        assertEquals("EXPIRED", policy.getStatus());
        assertEquals("EXPIRED", result.getStatus());

        verify(orderedPlanRepository, times(1)).save(op1);
        verify(policyRepository, times(1)).save(policy);
    }

    @Test
    void testCheckAndSetExpiration_OnePlanActive() {
        OrderedPlan op2 = new OrderedPlan();
        op2.setId("POL1-OP2");
        op2.setStatus("PAID");
        op2.setExpiredDate(LocalDate.now().plusDays(5)); 
        op2.setInsurancePlan(plan1);
        
        op1.setStatus("PAID");
        op1.setExpiredDate(LocalDate.now().minusDays(1)); 
        
        policy.setStatus("PAID");
        policy.setOrderedPlans(List.of(op1, op2));
        
        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));

        PolicyResponseDTO result = policyService.getPolicyById("POL1");

        assertEquals("EXPIRED", op1.getStatus());
        assertEquals("PAID", op2.getStatus());
        assertEquals("PAID", policy.getStatus());
        assertEquals("PAID", result.getStatus());
        
        verify(orderedPlanRepository, times(1)).save(op1); // op1 saved
        verify(orderedPlanRepository, never()).save(op2); // op2 not saved
        verify(policyRepository, never()).save(policy); // policy not saved
    }

    @Test
    void testCheckAndSetExpiration_OneClaimedOneExpired() {
        op1.setStatus("CLAIMED"); 
        op1.setExpiredDate(LocalDate.now().minusDays(1)); 
        
        policy.setStatus("FULLY_CLAIMED"); 
        policy.setOrderedPlans(List.of(op1));

        when(policyRepository.findById("POL1")).thenReturn(Optional.of(policy));
  
        PolicyResponseDTO result = policyService.getPolicyById("POL1");
        
  
        assertEquals("CLAIMED", op1.getStatus());
        assertEquals("FULLY_CLAIMED", policy.getStatus());
        
        verify(orderedPlanRepository, never()).save(any());
        verify(policyRepository, never()).save(any());
    }

    @Test
    void testGetAllPolicies() {
        op1.setStatus("PAID");
        op1.setExpiredDate(LocalDate.now().minusDays(1)); 
        policy.setStatus("PAID");

        when(policyRepository.findAll()).thenReturn(List.of(policy));

        List<PolicyResponseDTO> results = policyService.getAllPolicies();

        assertNotNull(results);
        assertEquals(1, results.size());

        assertEquals("EXPIRED", results.get(0).getStatus());
        assertEquals("EXPIRED", policy.getStatus());
        assertEquals("EXPIRED", op1.getStatus());

        verify(orderedPlanRepository, times(1)).save(op1);
        verify(policyRepository, times(1)).save(policy);
    }
}