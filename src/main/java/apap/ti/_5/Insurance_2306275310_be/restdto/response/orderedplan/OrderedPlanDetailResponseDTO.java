package apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan;

import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO untuk detail paket asuransi yang sudah dibeli (Ordered Plan).
 * Menyertakan daftar riwayat klaim yang pernah diajukan untuk paket ini.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderedPlanDetailResponseDTO {
    
    private String id;
    private String insurancePlanId;
    private String status;
    private String customerId;
    
    /** Tanggal paket kadaluarsa. */
    private LocalDate expiredDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /** Daftar riwayat klaim pada paket ini. */
    private List<ClaimSummaryResponseDTO> claims;
}