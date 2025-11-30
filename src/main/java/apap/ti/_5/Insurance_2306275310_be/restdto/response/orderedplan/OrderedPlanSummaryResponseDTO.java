package apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO ringkasan paket yang dibeli (untuk tampilan list di dalam detail Polis).
 * Tidak memuat detail klaim, hanya jumlahnya saja.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderedPlanSummaryResponseDTO {
    
    private String id;
    private String insurancePlanId;
    private String status;
    private LocalDate expiredDate;
    
    /** Jumlah klaim yang pernah diajukan. */
    private Integer claimsCount;
}