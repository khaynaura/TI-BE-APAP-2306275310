package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;

import java.util.List;

public interface ClaimService {

    List<ClaimSummaryResponseDTO> getAllClaimsFiltered(String status, String insurancePlanId);

    ClaimDetailResponseDTO getClaimById(String id);

    ClaimDetailResponseDTO createClaim(String orderedPlanId, CreateClaimRequestDTO createDTO);

    ClaimDetailResponseDTO processClaim(String claimId, ProcessClaimRequestDTO processDTO);

    boolean isClaimOwner(String claimId, String userId);
}