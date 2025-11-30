package apap.ti._5.Insurance_2306275310_be.restdto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * DTO generik untuk mengirim data grafik (Chart.js / ApexCharts).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataResponseDTO {
    
    /** Label sumbu X (misal: nama bulan, nama status). */
    private List<String> labels;
    
    /** Nilai data sumbu Y (misal: jumlah klaim, total pendapatan). */
    private List<Long> data;
}