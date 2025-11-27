package apap.ti._5.Insurance_2306275310_be.restdto.response.policy;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanSummaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResponseDTO {
    private String id; 
    private String bookingId;
    private String userId;
    private String billId; 
    private ServiceEnum service;
    private LocalDate startDate;
    private String status;
    private Integer totalPrice;
    private Integer totalCoverage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderedPlanSummaryResponseDTO> orderedPlans;
}