package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;

import java.util.List;

public interface PolicyService {

    PolicyResponseDTO createPolicy(CreatePolicyRequestDTO createDTO);

    List<PolicyResponseDTO> getAllPolicies();

    PolicyResponseDTO getPolicyById(String policyId);

    PolicyResponseDTO payPolicy(String policyId);

    List<PolicyResponseDTO> getPoliciesByUserId(String userId);

}