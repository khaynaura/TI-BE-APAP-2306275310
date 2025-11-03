package apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsurancePlanResponseDTO {
    private String id; 
    private String providerId;
    private String planName;
    private Integer price;
    private Integer coverage;
    private String coverageDetails;
    private List<ServiceEnum> applicableService;
    private Integer expiredByDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}