package apap.ti._5.Insurance_2306275310_be.restdto.response.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimSummaryResponseDTO {
    private String id; 
    private String orderedPlanId;
    private String planName;
    private String status;
    private Integer daysSinceClaimed; 
}