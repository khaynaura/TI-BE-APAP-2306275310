package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.repository.ClaimRepository;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.MonthlyOrderCount;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor; 
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final InsurancePlanRepository insurancePlanRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final OrderedPlanRepository orderedPlanRepository;

    @Override
    public HomeSummaryResponseDTO getHomeSummary(String userId, String role) {
        long totalPlans = 0;
        long totalPolicies = 0;
        long totalClaims = 0;

        if ("ROLE_CUSTOMER".equals(role)) {
            totalPlans = insurancePlanRepository.countByDeletedAtIsNull(); 
            totalPolicies = policyRepository.countByUserId(userId);
            totalClaims = claimRepository.countByCustomerUserId(userId);

        } else if ("ROLE_INSURANCE_PROVIDER".equals(role)) {
            totalPlans = insurancePlanRepository.countByProviderIdAndDeletedAtIsNull(userId);
            totalPolicies = policyRepository.countByProviderId(userId);
            totalClaims = claimRepository.countByProviderId(userId);

        } else {
            totalPlans = insurancePlanRepository.countByDeletedAtIsNull();
            totalPolicies = policyRepository.count();
            totalClaims = claimRepository.count();
        }

        return HomeSummaryResponseDTO.builder()
                .totalInsurancePlans(totalPlans)
                .totalPolicies(totalPolicies)
                .totalClaimsProcessed(totalClaims)
                .build();
    }

    @Override
    public ChartDataResponseDTO getChartStatistics(int timePeriod, String service, String providerId) {
       
        if (timePeriod <= 0) timePeriod = 3;

        LocalDateTime startDate = LocalDateTime.now().minusMonths(timePeriod - 1)
                                      .withDayOfMonth(1).toLocalDate().atStartOfDay();
        
        String serviceFilter = null;
        if (service != null && !service.isBlank() && !service.equalsIgnoreCase("All Services")) {
            serviceFilter = service;
        }
        
        List<MonthlyOrderCount> results = orderedPlanRepository.findMonthlyStats(startDate, serviceFilter, providerId);
   
        if (results == null) {
            results = new ArrayList<>();
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