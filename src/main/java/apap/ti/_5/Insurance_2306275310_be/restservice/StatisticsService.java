package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.ChartDataResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.statistics.HomeSummaryResponseDTO;

/**
 * Interface layanan untuk data Statistik dan Dashboard.
 */
public interface StatisticsService {

    /**
     * Mengambil ringkasan angka (Total Plan, Policy, Claim) untuk Dashboard Home.
     * Data disesuaikan dengan Role user yang login.
     *
     * @param userId ID User login.
     * @param role   Role User login.
     * @return DTO ringkasan statistik.
     */
    HomeSummaryResponseDTO getHomeSummary(String userId, String role);

    /**
     * Mengambil data grafik penjualan per bulan.
     *
     * @param timePeriod Periode waktu (bulan ke belakang).
     * @param service    Filter jenis layanan.
     * @param providerId Filter ID provider (Opsional/Null untuk Admin).
     * @return Data label dan value untuk grafik.
     */
    ChartDataResponseDTO getChartStatistics(int timePeriod, String service, String providerId);
}