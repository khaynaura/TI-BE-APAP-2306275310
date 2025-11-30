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
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InsurancePlanServiceImplTest {

    @Mock
    private InsurancePlanRepository insurancePlanRepository;

    // Kita mock builder karena dipanggil di constructor, 
    // meskipun webClient-nya sendiri mungkin tidak dipakai di logic method ini.
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private WebClient.Builder webClientBuilder;

    private InsurancePlanServiceImpl insurancePlanService;

    @BeforeEach
    void setUp() {
        insurancePlanService = new InsurancePlanServiceImpl(insurancePlanRepository);
    }

    // --- TEST: Create ---

    @Test
    void testCreateInsurancePlan() {
        CreateInsurancePlanRequestDTO req = new CreateInsurancePlanRequestDTO();
        req.setPlanName("Super Protection");
        req.setProviderId("P1");
        req.setPrice(100000);
        req.setCoverage(5000000);
        req.setExpiredByDays(30);

        // Simulasi sudah ada 10 plan, jadi plan baru harusnya ID-nya INS11
        when(insurancePlanRepository.countAll()).thenReturn(10L);
        when(insurancePlanRepository.save(any(InsurancePlan.class))).thenAnswer(i -> i.getArgument(0));

        InsurancePlanResponseDTO res = insurancePlanService.createInsurancePlan(req);

        assertEquals("INS11", res.getId());
        assertEquals("Super Protection", res.getPlanName());
        verify(insurancePlanRepository).save(any(InsurancePlan.class));
    }

    // --- TEST: Get All & Sorting Logic (PENTING) ---

    // @Test
    // void testGetAllPlans_SortedCorrectly() {
    //     // Kita tes logic sorting angka: INS2 harus sebelum INS10
    //     InsurancePlan p1 = new InsurancePlan(); p1.setId("INS2");
    //     InsurancePlan p2 = new InsurancePlan(); p2.setId("INS10");
    //     InsurancePlan p3 = new InsurancePlan(); p3.setId("INS1");
    //     // Kasus ID aneh (bukan angka) untuk tes try-catch fallback
    //     InsurancePlan pBad = new InsurancePlan(); pBad.setId("INS-X");

    //     List<InsurancePlan> unsortedList = Arrays.asList(p1, p2, p3, pBad);

    //     when(insurancePlanRepository.findAllByDeletedAtIsNull()).thenReturn(unsortedList);

    //     List<InsurancePlanResponseDTO> res = insurancePlanService.getAllPlans();

    //     // Urutan yang diharapkan: INS1, INS2, INS10, INS-X (Fallback string sort biasanya di akhir/awal tergantung string)
    //     // Logic code: num1 compare num2. Kalau error, string compare.
    //     // "INS1" -> 1
    //     // "INS2" -> 2
    //     // "INS10" -> 10
    //     // "INS-X" -> Error -> Fallback String comparison.
        
    //     assertEquals("INS1", res.get(0).getId());
    //     assertEquals("INS2", res.get(1).getId());
    //     assertEquals("INS10", res.get(2).getId());
    //     assertEquals("INS-X", res.get(3).getId()); // String compare fallback
    // }

    // --- TEST: Get By ID ---

    @Test
    void testGetPlanById_Found() {
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS1");
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan));

        InsurancePlanResponseDTO res = insurancePlanService.getPlanById("INS1");
        assertNotNull(res);
        assertEquals("INS1", res.getId());
    }

    @Test
    void testGetPlanById_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("XX")).thenReturn(Optional.empty());
        assertNull(insurancePlanService.getPlanById("XX"));
    }

    // --- TEST: Update ---

    @Test
    void testUpdateInsurancePlan_Success() {
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS1");
        plan.setPlanName("Old Name");

        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan));
        when(insurancePlanRepository.save(any())).thenReturn(plan);

        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        req.setId("INS1");
        req.setPlanName("New Name");

        InsurancePlanResponseDTO res = insurancePlanService.updateInsurancePlan(req);
        assertEquals("New Name", res.getPlanName());
    }

    @Test
    void testUpdateInsurancePlan_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.empty());
        UpdateInsurancePlanRequestDTO req = new UpdateInsurancePlanRequestDTO();
        req.setId("INS1");
        
        assertNull(insurancePlanService.updateInsurancePlan(req));
    }

    // --- TEST: Soft Delete (Validasi Expired) ---

    @Test
    void testSoftDeletePlan_Success() {
        // Skenario: Semua ordered plan sudah expired -> Boleh delete
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS1");
        
        OrderedPlan op1 = new OrderedPlan();
        op1.setExpiredDate(LocalDate.now().minusDays(10)); // Expired
        
        plan.setOrderedPlans(List.of(op1));

        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan));

        // Execute
        insurancePlanService.softDeletePlan("INS1");

        // Verify delete dipanggil
        verify(insurancePlanRepository).delete(plan);
    }

    @Test
    void testSoftDeletePlan_Fail_StillActive() {
        // Skenario: Masih ada ordered plan aktif -> Exception
        InsurancePlan plan = new InsurancePlan();
        plan.setId("INS1");

        OrderedPlan op1 = new OrderedPlan();
        op1.setExpiredDate(LocalDate.now().plusDays(30)); // Masih aktif
        
        plan.setOrderedPlans(List.of(op1));

        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("INS1")).thenReturn(Optional.of(plan));

        // Execute & Assert
        IllegalStateException e = assertThrows(IllegalStateException.class, () -> insurancePlanService.softDeletePlan("INS1"));
        assertTrue(e.getMessage().contains("belum expired"));
        
        // Pastikan TIDAK didelete
        verify(insurancePlanRepository, never()).delete(any());
    }

    @Test
    void testSoftDeletePlan_NotFound() {
        when(insurancePlanRepository.findByIdAndDeletedAtIsNull("XX")).thenReturn(Optional.empty());
        assertNull(insurancePlanService.softDeletePlan("XX"));
    }
    
    // --- TEST: Search & Filter ---

    @Test
    void testSearchPlansByName() {
        // 1. Keyword Null -> Get All
        when(insurancePlanRepository.findAllByDeletedAtIsNull()).thenReturn(new ArrayList<>());
        insurancePlanService.searchPlansByName(null);
        verify(insurancePlanRepository).findAllByDeletedAtIsNull();

        // 2. Keyword Exist -> Search
        when(insurancePlanRepository.findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase("Super"))
                .thenReturn(new ArrayList<>());
        insurancePlanService.searchPlansByName("Super");
        verify(insurancePlanRepository).findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase("Super");
    }

    @Test
    void testGetPlansByProviderId() {
        when(insurancePlanRepository.findAllByProviderIdAndDeletedAtIsNull("P1")).thenReturn(new ArrayList<>());
        insurancePlanService.getPlansByProviderId("P1");
        verify(insurancePlanRepository).findAllByProviderIdAndDeletedAtIsNull("P1");
    }

    @Test
    void testGetPlansByApplicableService() {
        when(insurancePlanRepository.findByApplicableServiceContainingAndDeletedAtIsNull(ServiceEnum.FLIGHT))
                .thenReturn(new ArrayList<>());
        insurancePlanService.getPlansByApplicableService(ServiceEnum.FLIGHT);
        verify(insurancePlanRepository).findByApplicableServiceContainingAndDeletedAtIsNull(ServiceEnum.FLIGHT);
    }
}