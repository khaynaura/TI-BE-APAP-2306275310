package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.repository.ClaimRepository;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.MonthlyOrderCount;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor; 
import org.springframework.stereotype.Service;

@Service
@Transactional
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final InsurancePlanRepository insurancePlanRepository;

    private final PolicyRepository policyRepository;

    private final ClaimRepository claimRepository;

    private final OrderedPlanRepository orderedPlanRepository;

    @Override
    public HomeSummaryResponseDTO getHomeSummary() {

        long totalPlans = insurancePlanRepository.countByDeletedAtIsNull();
        long totalPolicies = policyRepository.count();
        long totalClaims = claimRepository.count();

        return HomeSummaryResponseDTO.builder()
                .totalInsurancePlans(totalPlans)
                .totalPolicies(totalPolicies)
                .totalClaimsProcessed(totalClaims)
                .build();
    }

    @Override
    public ChartDataResponseDTO getChartStatistics(int timePeriod, String service) {

        LocalDateTime startDate = LocalDateTime.now().minusMonths(timePeriod - 1)
                                      .withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        List<MonthlyOrderCount> results;
        boolean filterByService = service != null && !service.isBlank() && !service.equalsIgnoreCase("All Services");
        
        if (filterByService) {
            results = orderedPlanRepository.findMonthlyOrderCountsByService(startDate, service);
        } else {
            results = orderedPlanRepository.findMonthlyOrderCounts(startDate);
        }
   
        return convertToChartDTO(results, timePeriod);
    }


    private ChartDataResponseDTO convertToChartDTO(List<MonthlyOrderCount> results, int timePeriod) {
  
        Map<Integer, Long> resultMap = results.stream()
                .collect(Collectors.toMap(
                    MonthlyOrderCount::getMonth,
                    MonthlyOrderCount::getCount
                ));

        List<String> labels = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        
        LocalDate currentDate = LocalDate.now();

        for (int i = timePeriod - 1; i >= 0; i--) {
            LocalDate monthDate = currentDate.minusMonths(i);
            int monthValue = monthDate.getMonthValue();
            String monthName = monthDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH); 

            labels.add(monthName);
            data.add(resultMap.getOrDefault(monthValue, 0L)); 
        }

        return ChartDataResponseDTO.builder()
                .labels(labels)
                .data(data)
                .build();
    }

}
