package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;

public interface StatisticsService {

    HomeSummaryResponseDTO getHomeSummary(String userId, String role);


    ChartDataResponseDTO getChartStatistics(int timePeriod, String service, String providerId);
}
