// package apap.ti._5.Insurance_2306275310_be.restservice;

// import apap.ti._5.Insurance_2306275310_be.repository.*;
// import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
// import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;

// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.mockito.MockedStatic;
// import org.mockito.Mockito;

// import java.time.LocalDate;
// import java.time.LocalDateTime;
// import java.time.Month;
// import java.util.ArrayList;
// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.*;

// @ExtendWith(MockitoExtension.class)
// public class StatisticsServiceTest {

//     @Mock
//     private InsurancePlanRepository insurancePlanRepository;

//     @Mock
//     private PolicyRepository policyRepository;

//     @Mock
//     private ClaimRepository claimRepository;

//     @Mock
//     private OrderedPlanRepository orderedPlanRepository;

//     @InjectMocks
//     private StatisticsServiceImpl statisticsService;

//     private static class MonthlyOrderCountImpl implements MonthlyOrderCount {
//         private final Integer month;
//         private final Long count;

//         public MonthlyOrderCountImpl(Integer month, Long count) {
//             this.month = month;
//             this.count = count;
//         }

//         @Override
//         public Integer getMonth() { return month; }
//         @Override
//         public Long getCount() { return count; }
//     }

//     @Test
//     void testGetHomeSummary() {
//         when(insurancePlanRepository.countByDeletedAtIsNull()).thenReturn(10L);
//         when(policyRepository.count()).thenReturn(50L);
//         when(claimRepository.count()).thenReturn(100L);

//         HomeSummaryResponseDTO result = statisticsService.getHomeSummary();

//         assertNotNull(result);
//         assertEquals(10L, result.getTotalInsurancePlans());
//         assertEquals(50L, result.getTotalPolicies());
//         assertEquals(100L, result.getTotalClaimsProcessed());
//         verify(insurancePlanRepository, times(1)).countByDeletedAtIsNull();
//         verify(policyRepository, times(1)).count();
//         verify(claimRepository, times(1)).count();
//     }

//     @Test
//     void testGetChartStatistics_AllServices() {

//         when(orderedPlanRepository.findMonthlyOrderCounts(any(LocalDateTime.class)))
//                 .thenReturn(new ArrayList<>());
 
//         statisticsService.getChartStatistics(6, "All Services");


//         verify(orderedPlanRepository, times(1)).findMonthlyOrderCounts(any(LocalDateTime.class));

//         verify(orderedPlanRepository, never()).findMonthlyOrderCountsByService(any(LocalDateTime.class), anyString());
//     }
    
//     @Test
//     void testGetChartStatistics_AllServices_Blank() {

//         when(orderedPlanRepository.findMonthlyOrderCounts(any(LocalDateTime.class)))
//                 .thenReturn(new ArrayList<>());

//         statisticsService.getChartStatistics(6, "   "); 

//         verify(orderedPlanRepository, times(1)).findMonthlyOrderCounts(any(LocalDateTime.class));
//         verify(orderedPlanRepository, never()).findMonthlyOrderCountsByService(any(LocalDateTime.class), anyString());
//     }

//     @Test
//     void testGetChartStatistics_SpecificService() {
//         String service = "FLIGHT";
//         when(orderedPlanRepository.findMonthlyOrderCountsByService(any(LocalDateTime.class), eq(service)))
//                 .thenReturn(new ArrayList<>());

//         statisticsService.getChartStatistics(6, service);

//         verify(orderedPlanRepository, times(1)).findMonthlyOrderCountsByService(any(LocalDateTime.class), eq(service));

//         verify(orderedPlanRepository, never()).findMonthlyOrderCounts(any(LocalDateTime.class));
//     }
// }