package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderedPlanServiceImpl implements OrderedPlanService {

    @Autowired
    private OrderedPlanRepository orderedPlanRepository;

    @Override
    public OrderedPlanDetailResponseDTO getOrderedPlanDetailById(String orderedPlanId) {
        OrderedPlan plan = orderedPlanRepository.findById(orderedPlanId)
                .orElseThrow(() -> new RuntimeException("Ordered Plan not found"));
        
        return convertToDetailDTO(plan);
    }


    private OrderedPlanDetailResponseDTO convertToDetailDTO(OrderedPlan plan) {

        List<ClaimSummaryResponseDTO> claimDTOs = plan.getClaims().stream()
                .map(this::convertClaimToSummaryDTO)
                .collect(Collectors.toList());

        return OrderedPlanDetailResponseDTO.builder()
                .id(plan.getId())
                .insurancePlanId(plan.getInsurancePlan().getId())
                .status(plan.getStatus())
                .expiredDate(plan.getExpiredDate())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .claims(claimDTOs) // list DTO claim
                .build();
    }


    private ClaimSummaryResponseDTO convertClaimToSummaryDTO(Claim claim) {
        long daysSinceClaimed = 0;
        if (claim.getStatus().equals("WAITING_FOR_REVIEW")) {
            daysSinceClaimed = ChronoUnit.DAYS.between(claim.getCreatedAt().toLocalDate(), LocalDate.now());
        }

        return ClaimSummaryResponseDTO.builder()
                .id(claim.getId())
                .orderedPlanId(claim.getOrderedPlan().getId())
                .planName(claim.getOrderedPlan().getInsurancePlan().getPlanName())
                .status(claim.getStatus())
                .daysSinceClaimed((int) daysSinceClaimed)
                .build();
    }
}