package apap.ti._5.Insurance_2306275310_be.restdto.response.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO untuk ringkasan angka di Dashboard Admin/User.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeSummaryResponseDTO {
    
    private long totalInsurancePlans;
    private long totalPolicies;
    private long totalClaimsProcessed;
}