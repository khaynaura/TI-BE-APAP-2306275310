package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementasi dari interface {@link OrderedPlanService}.
 * Menangani logika bisnis untuk melihat detail OrderedPlan beserta klaim yang terkait.
 */
@Service
@Transactional
@AllArgsConstructor
public class OrderedPlanServiceImpl implements OrderedPlanService {

    private final OrderedPlanRepository orderedPlanRepository;

    /**
     * Mengambil detail OrderedPlan berdasarkan ID, termasuk daftar klaim terkait.
     *
     * @param orderedPlanId ID dari Ordered Plan yang dicari.
     * @return {@link OrderedPlanDetailResponseDTO} berisi detail plan dan list klaim.
     * @throws RuntimeException Jika Ordered Plan tidak ditemukan.
     */
    @Override
    public OrderedPlanDetailResponseDTO getOrderedPlanDetailById(String orderedPlanId) {
        OrderedPlan plan = orderedPlanRepository.findById(orderedPlanId)
                .orElseThrow(() -> new RuntimeException("Ordered Plan not found"));

        return convertToDetailDTO(plan);
    }

    /**
     * Mengonversi entitas OrderedPlan menjadi DTO response detail.
     *
     * @param plan Entitas OrderedPlan.
     * @return Objek DTO detail.
     */
    private OrderedPlanDetailResponseDTO convertToDetailDTO(OrderedPlan plan) {
        List<ClaimSummaryResponseDTO> claimDTOs = plan.getClaims().stream()
                .map(this::convertClaimToSummaryDTO)
                .collect(Collectors.toList());

        return OrderedPlanDetailResponseDTO.builder()
                .id(plan.getId())
                .insurancePlanId(plan.getInsurancePlan().getId())
                .status(plan.getStatus())
                .customerId(plan.getPolicy().getUserId())
                .expiredDate(plan.getExpiredDate())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .claims(claimDTOs) // list DTO claim
                .build();
    }

    /**
     * Mengonversi entitas Claim menjadi DTO summary.
     * Menghitung durasi hari sejak klaim dibuat jika statusnya masih WAITING_FOR_REVIEW.
     *
     * @param claim Entitas Claim.
     * @return Objek DTO summary klaim.
     */
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