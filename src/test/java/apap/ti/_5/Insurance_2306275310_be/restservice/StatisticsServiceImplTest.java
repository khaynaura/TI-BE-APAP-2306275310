package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.repository.*;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceImplTest {

    @Mock
    private InsurancePlanRepository insurancePlanRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private OrderedPlanRepository orderedPlanRepository;

    @InjectMocks
    private StatisticsServiceImpl statisticsService;

    // --- TEST: getHomeSummary (Logic Role) ---

    @Test
    void testGetHomeSummary_Customer() {
        String userId = "U1";
        String role = "ROLE_CUSTOMER";

        when(insurancePlanRepository.countByDeletedAtIsNull()).thenReturn(10L);
        when(policyRepository.countByUserId(userId)).thenReturn(5L);
        when(claimRepository.countByCustomerUserId(userId)).thenReturn(2L);

        HomeSummaryResponseDTO res = statisticsService.getHomeSummary(userId, role);

        assertEquals(10L, res.getTotalInsurancePlans()); // Logic: semua plan aktif
        assertEquals(5L, res.getTotalPolicies());        // Logic: policy milik user
        assertEquals(2L, res.getTotalClaimsProcessed()); // Logic: claim milik user
    }

    @Test
    void testGetHomeSummary_Provider() {
        String providerId = "P1";
        String role = "ROLE_INSURANCE_PROVIDER";

        when(insurancePlanRepository.countByProviderIdAndDeletedAtIsNull(providerId)).thenReturn(8L);
        when(policyRepository.countByProviderId(providerId)).thenReturn(4L);
        when(claimRepository.countByProviderId(providerId)).thenReturn(3L);

        HomeSummaryResponseDTO res = statisticsService.getHomeSummary(providerId, role);

        assertEquals(8L, res.getTotalInsurancePlans()); // Logic: plan milik provider
        assertEquals(4L, res.getTotalPolicies());       // Logic: policy yg beli plan provider ini
        assertEquals(3L, res.getTotalClaimsProcessed());// Logic: claim masuk ke provider ini
    }

    @Test
    void testGetHomeSummary_Admin() {
        String userId = "ADMIN";
        String role = "ROLE_ADMIN"; // Bukan Customer/Provider

        when(insurancePlanRepository.countByDeletedAtIsNull()).thenReturn(100L);
        when(policyRepository.count()).thenReturn(50L);
        when(claimRepository.count()).thenReturn(20L);

        HomeSummaryResponseDTO res = statisticsService.getHomeSummary(userId, role);

        assertEquals(100L, res.getTotalInsurancePlans());
        assertEquals(50L, res.getTotalPolicies());
        assertEquals(20L, res.getTotalClaimsProcessed());
    }

    // --- TEST: getChartStatistics (Logic Chart & Date) ---

    @Test
    void testGetChartStatistics_DefaultPeriod() {
        // Case: timePeriod <= 0, should default to 3
        int period = 0; 
        
        // Mock data kosong dari DB
        when(orderedPlanRepository.findMonthlyStats(any(), any(), any())).thenReturn(new ArrayList<>());

        ChartDataResponseDTO res = statisticsService.getChartStatistics(period, "All Services", "P1");

        // Assert size labels harus 3 (Default logic)
        assertEquals(3, res.getLabels().size());
        assertEquals(3, res.getData().size());
        
        // Verify label bulan terakhir adalah bulan ini
        String currentMonth = LocalDate.now().getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        assertEquals(currentMonth, res.getLabels().get(2)); // Index terakhir
    }

    @Test
    void testGetChartStatistics_WithDataAndFilter() {
        int period = 5;
        String service = "FLIGHT";
        String providerId = "P1";

        // Mock Interface Projection (MonthlyOrderCount)
        // Karena MonthlyOrderCount itu Interface, kita harus mock method-nya
        MonthlyOrderCount m1 = mock(MonthlyOrderCount.class);
        when(m1.getMonth()).thenReturn(LocalDate.now().getMonthValue()); // Data bulan ini
        when(m1.getCount()).thenReturn(10L);

        MonthlyOrderCount m2 = mock(MonthlyOrderCount.class);
        when(m2.getMonth()).thenReturn(LocalDate.now().minusMonths(1).getMonthValue()); // Data bulan lalu
        when(m2.getCount()).thenReturn(5L);

        List<MonthlyOrderCount> dbResult = List.of(m1, m2);

        when(orderedPlanRepository.findMonthlyStats(any(), eq("FLIGHT"), eq("P1"))).thenReturn(dbResult);

        // Execute
        ChartDataResponseDTO res = statisticsService.getChartStatistics(period, service, providerId);

        // Assert
        assertEquals(5, res.getLabels().size()); // Period 5
        
        // Cek data di index terakhir (bulan ini)
        Long dataThisMonth = res.getData().get(4); 
        assertEquals(10L, dataThisMonth);

        // Cek data di index sebelumnya (bulan lalu)
        Long dataLastMonth = res.getData().get(3);
        assertEquals(5L, dataLastMonth);
        
        // Cek data bulan lampau yg ga ada di DB (harus 0)
        assertEquals(0L, res.getData().get(0));
    }

    @Test
    void testGetChartStatistics_NullResultSafeguard() {
        // Case: Repository return null (bukan empty list), service harus handle biar ga NPE
        when(orderedPlanRepository.findMonthlyStats(any(), any(), any())).thenReturn(null);

        ChartDataResponseDTO res = statisticsService.getChartStatistics(3, null, "P1");

        assertNotNull(res.getLabels());
        assertNotNull(res.getData());
        assertEquals(3, res.getData().size());
        assertEquals(0L, res.getData().get(0)); // Default 0
    }
}