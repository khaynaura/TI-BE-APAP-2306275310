package apap.ti._5.Insurance_2306275310_be.restdto.response.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk menampilkan ringkasan klaim dalam bentuk daftar/tabel.
 * Hanya memuat informasi penting agar ringan saat dimuat.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSummaryResponseDTO {
    
    /** ID unik klaim. */
    private String id;
    
    /** ID dari pesanan paket asuransi terkait. */
    private String orderedPlanId;
    
    /** Nama paket asuransi. */
    private String planName;
    
    /** Status klaim. */
    private String status;
    
    /** Jumlah hari yang telah berlalu sejak klaim diajukan. */
    private Integer daysSinceClaimed;
}