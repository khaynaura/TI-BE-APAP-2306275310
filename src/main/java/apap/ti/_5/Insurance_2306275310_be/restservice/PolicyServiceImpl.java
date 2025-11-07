package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import apap.ti._5.Insurance_2306275310_be.model.Policy;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.OrderedPlanRepository;
import apap.ti._5.Insurance_2306275310_be.repository.PolicyRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanSummaryResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor; 
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;

    private final InsurancePlanRepository insurancePlanRepository;

    private final OrderedPlanRepository orderedPlanRepository;

    @Override
    public PolicyResponseDTO createPolicy(CreatePolicyRequestDTO createDTO) {

        List<InsurancePlan> plans = insurancePlanRepository.findAllById(createDTO.getInsurancePlanIds());
        

        if (plans.size() != createDTO.getInsurancePlanIds().size()) {
            throw new RuntimeException("One or more Insurance Plan IDs are invalid.");
        }

        int totalPrice = plans.stream().mapToInt(InsurancePlan::getPrice).sum();
        int totalCoverage = plans.stream().mapToInt(InsurancePlan::getCoverage).sum();


        Policy policy = new Policy();

        policy.setId("POL" + (policyRepository.count() + 1));
        policy.setUserId(createDTO.getUserId());
        policy.setBookingId(createDTO.getBookingId());
        policy.setService(createDTO.getService());
        policy.setStartDate(LocalDate.now()); 
        policy.setStatus("CREATED"); 
        policy.setTotalPrice(totalPrice);
        policy.setTotalCoverage(totalCoverage);

        List<OrderedPlan> orderedPlans = new ArrayList<>();
        int i = 1;
        for (InsurancePlan plan : plans) {
            OrderedPlan op = new OrderedPlan();
            op.setId(policy.getId() + "-OP" + i);
            op.setStatus("ORDERED"); 
            op.setExpiredDate(policy.getStartDate().plusDays(plan.getExpiredByDays()));
            op.setInsurancePlan(plan);
            op.setPolicy(policy);
            orderedPlans.add(op);
            i++;
        }

        policy.setOrderedPlans(new ArrayList<>()); 
        Policy savedPolicy = policyRepository.save(policy);

        List<OrderedPlan> savedOrderedPlans = orderedPlanRepository.saveAll(orderedPlans);
        savedPolicy.setOrderedPlans(savedOrderedPlans);

        return convertToResponseDTO(savedPolicy);
    }

    @Override
    public List<PolicyResponseDTO> getAllPolicies() {
        List<Policy> allPolicies = policyRepository.findAll();
        
       
        allPolicies.forEach(this::checkAndSetExpiration);
        
        return allPolicies.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PolicyResponseDTO getPolicyById(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        
        checkAndSetExpiration(policy);

        return convertToResponseDTO(policy);
    }

    @Override
    public PolicyResponseDTO payPolicy(String policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));

        if (!policy.getStatus().equals("CREATED")) {
            throw new IllegalStateException("Policy cannot be paid. Status is: " + policy.getStatus());
        }

        policy.setStatus("PAID");
        policy.setUpdatedAt(LocalDateTime.now());

        List<OrderedPlan> orderedPlans = policy.getOrderedPlans();
        for (OrderedPlan op : orderedPlans) {
            op.setStatus("PAID");
            op.setUpdatedAt(LocalDateTime.now());
        }
        orderedPlanRepository.saveAll(orderedPlans);
        
        Policy savedPolicy = policyRepository.save(policy);
        return convertToResponseDTO(savedPolicy);
    }

    private void checkAndSetExpiration(Policy policy) {
        if (policy.getStatus().equals("EXPIRED")) return; 

        List<OrderedPlan> plans = policy.getOrderedPlans();
        boolean hasUnclaimedNonExpired = false;
        boolean hasClaimed = false;

        for (OrderedPlan op : plans) {
            if (op.getStatus().equals("CLAIMED")) {
                hasClaimed = true; 
            }

            if (op.getExpiredDate().isBefore(LocalDate.now()) && !op.getStatus().equals("CLAIMED")) {
                if (!op.getStatus().equals("EXPIRED")) { 
                    op.setStatus("EXPIRED");
                    orderedPlanRepository.save(op);
                }
            }
            
            if (!op.getStatus().equals("CLAIMED") && !op.getStatus().equals("EXPIRED")) {
                hasUnclaimedNonExpired = true;
            }
        }

        if (!hasClaimed && !hasUnclaimedNonExpired) {
            policy.setStatus("EXPIRED");
            policyRepository.save(policy);
        }
    }

    private PolicyResponseDTO convertToResponseDTO(Policy policy) {
        List<OrderedPlanSummaryResponseDTO> opDTOs = policy.getOrderedPlans().stream()
                .map(this::convertOpToSummaryDTO)
                .collect(Collectors.toList());

        return PolicyResponseDTO.builder()
                .id(policy.getId())
                .bookingId(policy.getBookingId())
                .userId(policy.getUserId())
                .service(policy.getService())
                .startDate(policy.getStartDate())
                .status(policy.getStatus())
                .totalPrice(policy.getTotalPrice())
                .totalCoverage(policy.getTotalCoverage())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .orderedPlans(opDTOs)
                .build();
    }

    private OrderedPlanSummaryResponseDTO convertOpToSummaryDTO(OrderedPlan op) {
        return OrderedPlanSummaryResponseDTO.builder()
                .id(op.getId())
                .insurancePlanId(op.getInsurancePlan().getId())
                .status(op.getStatus())
                .expiredDate(op.getExpiredDate())
                .claimsCount(op.getClaims() != null ? op.getClaims().size() : 0)
                .build();
    }
}