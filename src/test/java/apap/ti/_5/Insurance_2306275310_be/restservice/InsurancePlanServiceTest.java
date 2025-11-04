package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InsurancePlanServiceTest {

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    @InjectMocks
    private InsurancePlanServiceImpl insurancePlanService;

    private InsurancePlan plan1;
    private InsurancePlan plan10;
    private CreateInsurancePlanRequestDTO createDTO;
    private UpdateInsurancePlanRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        plan1 = InsurancePlan.builder()
                .id("INS1")
                .planName("Basic Plan")
                .price(100)
                .coverage(1000)
                .coverageDetails("Basic coverage")
                .applicableService(List.of(ServiceEnum.FLIGHT))
                .expiredByDays(30)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        plan10 = InsurancePlan.builder()
                .id("INS10")
                .planName("Premium Plan")
                .price(500)
                .coverage(10000)
                .expiredByDays(60)
                .build();

        createDTO = CreateInsurancePlanRequestDTO.builder()
                .planName("New Plan")
                .providerId("P-1")
                .price(200)
                .coverage(2000)
                .coverageDetails("New details")
                .applicableService(List.of(ServiceEnum.ACCOMMODATION))
                .expiredByDays(45)
                .build();
        
        updateDTO = UpdateInsurancePlanRequestDTO.builder()
                .id("INS1")
                .planName("Updated Basic Plan")
                .price(110)
                .coverage(1100)
                .coverageDetails("Updated details")
                .applicableService(List.of(ServiceEnum.FLIGHT, ServiceEnum.RENTALS))
                .expiredByDays(35)
                .build();
    }

    @Test
    void testCreateInsurancePlan() {
        when(insurancePlanRepository.countAll()).thenReturn(10L);
        
        InsurancePlan savedPlan = InsurancePlan.builder()
            .id("INS11") 
            .planName(createDTO.getPlanName())
            .price(createDTO.getPrice())
            // ... set other fields from createDTO
            .build();
            
        when(insurancePlanRepository.save(any(InsurancePlan.class))).thenReturn(savedPlan);

        InsurancePlanResponseDTO result = insurancePlanService.createInsurancePlan(createDTO);

        assertNotNull(result);
        assertEquals("INS11", result.getId());
        assertEquals(createDTO.getPlanName(), result.getPlanName());
        assertEquals(createDTO.getPrice(), result.getPrice());
        verify(insurancePlanRepository, times(1)).countAll();
        verify(insurancePlanRepository, times(1)).save(any(InsurancePlan.class));
    }

    @Test
    void testGetAllPlans_ShouldReturnSorted() {

        when(insurancePlanRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(plan10, plan1));

        List<InsurancePlanResponseDTO> results = insurancePlanService.getAllPlans();

        assertNotNull(results);
        assertEquals(2, results.size());

        assertEquals("INS1", results.get(0).getId());
        assertEquals("INS10", results.get(1).getId());
        verify(insurancePlanRepository, times(1)).findAllByDeletedAtIsNull();
    }

    @Test
    void testGetPlanById_Success() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan1));

        InsurancePlanResponseDTO result = insurancePlanService.getPlanById("INS1");

        assertNotNull(result);
        assertEquals("INS1", result.getId());
        assertEquals("Basic Plan", result.getPlanName());
        verify(insurancePlanRepository, times(1)).findByIdAndDeletedAtIsNull("INS1");
    }

    @Test
    void testGetPlanById_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS99")).thenReturn(Optional.empty());

        InsurancePlanResponseDTO result = insurancePlanService.getPlanById("INS99");

        assertNull(result);
        verify(insurancePlanRepository, times(1)).findByIdAndDeletedAtIsNull("INS99");
    }

    @Test
    void testUpdateInsurancePlan_Success() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan1));
        
        InsurancePlan updatedPlan = plan1.toBuilder()
            .planName(updateDTO.getPlanName())
            .price(updateDTO.getPrice())
            .coverage(updateDTO.getCoverage())
            .coverageDetails(updateDTO.getCoverageDetails())
            .applicableService(updateDTO.getApplicableService())
            .expiredByDays(updateDTO.getExpiredByDays())
            .build();
        
        when(insurancePlanRepository.save(any(InsurancePlan.class))).thenReturn(updatedPlan);

        InsurancePlanResponseDTO result = insurancePlanService.updateInsurancePlan(updateDTO);

        assertNotNull(result);
        assertEquals(updateDTO.getId(), result.getId());
        assertEquals(updateDTO.getPlanName(), result.getPlanName()); // Updated name
        assertEquals(updateDTO.getPrice(), result.getPrice()); // Updated price
        verify(insurancePlanRepository, times(1)).findByIdAndDeletedAtIsNull("INS1");
        verify(insurancePlanRepository, times(1)).save(any(InsurancePlan.class));
    }

    @Test
    void testUpdateInsurancePlan_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull(anyString())).thenReturn(Optional.empty());

        InsurancePlanResponseDTO result = insurancePlanService.updateInsurancePlan(updateDTO);

        assertNull(result);
        verify(insurancePlanRepository, times(1)).findByIdAndDeletedAtIsNull(anyString());
        verify(insurancePlanRepository, never()).save(any(InsurancePlan.class));
    }

    @Test
    void testSoftDeletePlan_Success_AllOrderedPlansExpired() {
        OrderedPlan expiredPlan = new OrderedPlan();
        expiredPlan.setExpiredDate(LocalDate.now().minusDays(1));
        
        plan1.setOrderedPlans(List.of(expiredPlan));
        
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan1));

        InsurancePlanResponseDTO result = insurancePlanService.softDeletePlan("INS1");

        assertNotNull(result);
        assertEquals("INS1", result.getId());
        verify(insurancePlanRepository, times(1)).delete(plan1);
    }
    
    @Test
    void testSoftDeletePlan_Success_NoOrderedPlans() {
        plan1.setOrderedPlans(new ArrayList<>()); 
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan1));

        InsurancePlanResponseDTO result = insurancePlanService.softDeletePlan("INS1");

        assertNotNull(result);
        verify(insurancePlanRepository, times(1)).delete(plan1);
    }
    
    @Test
    void testSoftDeletePlan_Fail_ActiveOrderedPlanExists() {
        OrderedPlan activePlan = new OrderedPlan();
        activePlan.setExpiredDate(LocalDate.now().plusDays(10)); 
        
        plan1.setOrderedPlans(List.of(activePlan));
        
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan1));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            insurancePlanService.softDeletePlan("INS1");
        });

        assertEquals("Plan tidak dapat dihapus karena satu atau lebih Ordered Plan terkait belum expired.", exception.getMessage());
        
        verify(insurancePlanRepository, never()).delete(any(InsurancePlan.class));
    }

    @Test
    void testSoftDeletePlan_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS99")).thenReturn(Optional.empty());

        InsurancePlanResponseDTO result = insurancePlanService.softDeletePlan("INS99");

        assertNull(result);
        verify(insurancePlanRepository, never()).delete(any(InsurancePlan.class));
    }

    @Test
    void testSearchPlansByName_WithKeyword() {
        when(insurancePlanRepository.findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase("Basic"))
                .thenReturn(List.of(plan1));

        List<InsurancePlanResponseDTO> results = insurancePlanService.searchPlansByName("Basic");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("INS1", results.get(0).getId());
        verify(insurancePlanRepository, times(1)).findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase("Basic");
        verify(insurancePlanRepository, never()).findAllByDeletedAtIsNull();
    }
    
    @Test
    void testSearchPlansByName_WithKeyword_ShouldSort() {
        when(insurancePlanRepository.findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase("Plan"))
                .thenReturn(List.of(plan10, plan1));
        List<InsurancePlanResponseDTO> results = insurancePlanService.searchPlansByName("Plan");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("INS1", results.get(0).getId());
        assertEquals("INS10", results.get(1).getId());
    }

    @Test
    void testSearchPlansByName_BlankKeyword() {
        when(insurancePlanRepository.findAllByDeletedAtIsNull()).thenReturn(List.of(plan10, plan1)); 
        List<InsurancePlanResponseDTO> results = insurancePlanService.searchPlansByName("   ");

        assertNotNull(results);
        assertEquals(2, results.size());
        assertEquals("INS1", results.get(0).getId());
        assertEquals("INS10", results.get(1).getId());
        verify(insurancePlanRepository, never()).findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase(anyString());
        verify(insurancePlanRepository, times(1)).findAllByDeletedAtIsNull();
    }
    
    @Test
    void testGetPlansByApplicableService() {
        ServiceEnum serviceToFind = ServiceEnum.FLIGHT;
        when(insurancePlanRepository.findByApplicableServiceContainingAndDeletedAtIsNull(serviceToFind))
                .thenReturn(List.of(plan1));

        List<InsurancePlanResponseDTO> results = insurancePlanService.getPlansByApplicableService(serviceToFind);

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("INS1", results.get(0).getId());
        verify(insurancePlanRepository, times(1)).findByApplicableServiceContainingAndDeletedAtIsNull(serviceToFind);
    }
}