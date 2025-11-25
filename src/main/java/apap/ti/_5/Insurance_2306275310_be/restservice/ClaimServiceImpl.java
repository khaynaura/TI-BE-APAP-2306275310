package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.Policy;
import apap.ti._5.Insurance_2306275310_be.repository.ClaimRepository;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor; 

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;

    private final OrderedPlanRepository orderedPlanRepository;

    private final PolicyRepository policyRepository;

    @Override
    public List<ClaimSummaryResponseDTO> getAllClaimsFiltered(String status, String insurancePlanId) {
        List<Claim> claims;
        
        // Cek apakah ada filter
        boolean filterByStatus = status != null && !status.isBlank() && !status.equalsIgnoreCase("All Statuses");
        boolean filterByPlan = insurancePlanId != null && !insurancePlanId.isBlank() && !insurancePlanId.equalsIgnoreCase("All Insurance Plans");

        if (filterByStatus && filterByPlan) {
            claims = claimRepository.findAllByStatusAndOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(status, insurancePlanId);
        
        } else if (filterByStatus) {
            claims = claimRepository.findAllByStatusOrderByCreatedAtDesc(status);
        
        } else if (filterByPlan) {
            claims = claimRepository.findAllByOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(insurancePlanId);
        
        } else {
            claims = claimRepository.findAllByOrderByCreatedAtDesc();
        }

        return claims.stream()
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClaimDetailResponseDTO getClaimById(String id) {
        Claim claim = claimRepository.findById(id).orElse(null);
        if (claim == null) {
            throw new RuntimeException("Claim not found");
        }
        return convertToDetailDTO(claim);
    }

    @Override
    public ClaimDetailResponseDTO createClaim(String orderedPlanId, CreateClaimRequestDTO createDTO) {
        OrderedPlan orderedPlan = orderedPlanRepository.findById(orderedPlanId).orElse(null);
        if (orderedPlan == null) {
            throw new RuntimeException("Ordered Plan not found");
        }

        // syarat: Claim tidak dapat dibuat jika sudah melewati expiredDate 
        if (orderedPlan.getExpiredDate().isBefore(LocalDate.now())) {
            throw new IllegalStateException("Cannot submit claim, ordered plan has expired.");
        }

        // tidak boleh claim jika status OrderedPlan sudah CLAIMED (sudah pernah di-ACCEPT)
        if (orderedPlan.getStatus().equals("CLAIMED")) { 
            throw new IllegalStateException("Cannot submit claim, this plan has already been claimed.");
        }

        // tidak boleh claim jika ada claim lain yg masih WAITING_FOR_REVIEW
        if (orderedPlan.getStatus().equals("WAITING_FOR_REVIEW")) { 
             throw new IllegalStateException("Cannot submit new claim, another claim is already waiting for review.");
        }
        
        // tidak boleh claim jika jumlah total claim sudah 3
        if (orderedPlan.getClaims().size() >= 3) {
            throw new IllegalStateException("Cannot submit new claim, the maximum number of 3 claims has been reached.");
        }

        Claim claim = new Claim();
        claim.setId(orderedPlan.getId() + "-CLAIM" + (orderedPlan.getClaims().size() + 1));
        claim.setProof(createDTO.getProof());
        claim.setStatus("WAITING_FOR_REVIEW"); 
        claim.setOrderedPlan(orderedPlan);

        Claim savedClaim = claimRepository.save(claim);

        // Update status OrderedPlan menjadi WAITING_FOR_REVIEW
        orderedPlan.setStatus("WAITING_FOR_REVIEW");
        orderedPlanRepository.save(orderedPlan);

        return convertToDetailDTO(savedClaim);
    }

    @Override
    public ClaimDetailResponseDTO processClaim(String claimId, ProcessClaimRequestDTO processDTO) {

        Claim claim = claimRepository.findById(claimId).orElse(null);
        if (claim == null) {
            throw new RuntimeException("Claim not found");
        }
        
        if (!claim.getStatus().equals("WAITING_FOR_REVIEW")) {
            throw new IllegalStateException("Claim is not waiting for review.");
        }

        OrderedPlan orderedPlan = claim.getOrderedPlan();

        if (processDTO.getIsAccepted()) {

            claim.setStatus("ACCEPTED"); 
            claim.setAcceptedNote(processDTO.getAcceptedNote());
            claim.setAcceptedTimestamp(LocalDateTime.now());

            orderedPlan.setStatus("CLAIMED"); 
            
            updatePolicyStatus(orderedPlan.getPolicy());

        } else {
            claim.setStatus("REJECTED"); 
            claim.setRejectionReason(processDTO.getRejectionReason());
            claim.setRejectionDescription(processDTO.getRejectionDescription());
            claim.setRejectionTimestamp(LocalDateTime.now());

            long rejectedCount = orderedPlan.getClaims().stream()
                    .filter(c -> c.getStatus().equals("REJECTED"))
                    .count();
            
            if (rejectedCount >= 3) {
                orderedPlan.setStatus("REJECTED"); 
            } else {
                // Jika < 3, kembalikan status ke PAID agar bisa di-claim lagi
                orderedPlan.setStatus("PAID"); 
            }
        }

        orderedPlanRepository.save(orderedPlan);
        Claim processedClaim = claimRepository.save(claim);
        return convertToDetailDTO(processedClaim);
    }


    private void updatePolicyStatus(Policy policy) {

        if (policy == null) return;

        List<OrderedPlan> relatedPlans = policy.getOrderedPlans();
        
        boolean allClaimed = relatedPlans.stream()
                .allMatch(op -> op.getStatus().equals("CLAIMED"));

        if (allClaimed) {
            policy.setStatus("FULLY_CLAIMED");
        } else {
            boolean anyClaimed = relatedPlans.stream()
                    .anyMatch(op -> op.getStatus().equals("CLAIMED"));
            if (anyClaimed) {
                policy.setStatus("PARTIALLY_CLAIMED");
            }
        }
        policyRepository.save(policy);
    }

    private ClaimSummaryResponseDTO convertToSummaryDTO(Claim claim) {
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

    private ClaimDetailResponseDTO convertToDetailDTO(Claim claim) {
        return ClaimDetailResponseDTO.builder()
                .id(claim.getId())
                .status(claim.getStatus())
                .proof(claim.getProof())
                .rejectionReason(claim.getRejectionReason())
                .rejectionDescription(claim.getRejectionDescription())
                .rejectionTimestamp(claim.getRejectionTimestamp())
                .acceptedNote(claim.getAcceptedNote())
                .acceptedTimestamp(claim.getAcceptedTimestamp())
                .build();
    }

    @Override
    public boolean isClaimOwner(String claimId, String userId) {
        // 1. Cari Claim berdasarkan claimId
        Claim claim = claimRepository.findById(claimId).orElse(null); 
        
        // Jika claim tidak ada, otomatis bukan pemilik (return false)
        if (claim == null) {
            return false;
        }
    
        // 2. Cek alur relasi: Claim -> OrderedPlan -> Policy -> User ID
        // Kita bandingkan User ID di Policy dengan userId yang sedang login
        return claim.getOrderedPlan().getPolicy().getUserId().equals(userId);
    }
    
}