package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.repository.InsurancePlanRepository;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class InsurancePlanServiceImpl implements InsurancePlanService {

    private final InsurancePlanRepository insurancePlanRepository;
    private final WebClient webClient;

    @Value("${profile.service.url:http://localhost:8082/api}")
    private String profileServiceUrl;

    public InsurancePlanServiceImpl(InsurancePlanRepository insurancePlanRepository, 
                                    WebClient.Builder webClientBuilder) {
        this.insurancePlanRepository = insurancePlanRepository;
        this.webClient = webClientBuilder.build();
    }

    @Override
    public InsurancePlanResponseDTO createInsurancePlan(CreateInsurancePlanRequestDTO createDTO) {
        long totalPlans = insurancePlanRepository.countAll();
        String newId = "INS" + (totalPlans + 1);

        InsurancePlan plan = InsurancePlan.builder()
                .id(newId)
                .providerId(createDTO.getProviderId())
                .planName(createDTO.getPlanName())
                .price(createDTO.getPrice())
                .coverage(createDTO.getCoverage())
                .coverageDetails(createDTO.getCoverageDetails())
                .applicableService(createDTO.getApplicableService())
                .expiredByDays(createDTO.getExpiredByDays())
                .build();

        InsurancePlan savedPlan = insurancePlanRepository.save(plan);
        return convertToResponseDTO(savedPlan);
    }

    @Override
    public List<InsurancePlanResponseDTO> getAllPlans() {
        return insurancePlanRepository.findAllByDeletedAtIsNull().stream()
                .sorted((p1, p2) -> {
                    // Extract number from "INS1", "INS2", etc.
                    try {
                        int num1 = Integer.parseInt(p1.getId().substring(3));
                        int num2 = Integer.parseInt(p2.getId().substring(3));
                        return Integer.compare(num1, num2);
                    } catch (NumberFormatException e) {
                        return p1.getId().compareTo(p2.getId());
                    }
                })
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InsurancePlanResponseDTO getPlanById(String id) {
        InsurancePlan plan = insurancePlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElse(null);

        if (plan == null) {
            return null;
        }
        return convertToResponseDTO(plan);
    }

    @Override
    public InsurancePlanResponseDTO updateInsurancePlan(UpdateInsurancePlanRequestDTO updateDTO) {
        InsurancePlan plan = insurancePlanRepository.findByIdAndDeletedAtIsNull(updateDTO.getId())
                .orElse(null);

        if (plan == null) {
            return null;
        }

        plan.setPlanName(updateDTO.getPlanName());
        plan.setPrice(updateDTO.getPrice());
        plan.setCoverage(updateDTO.getCoverage());
        plan.setCoverageDetails(updateDTO.getCoverageDetails());
        plan.setApplicableService(updateDTO.getApplicableService());
        plan.setExpiredByDays(updateDTO.getExpiredByDays());
        // plan.setUpdatedAt(LocalDateTime.now()); // Otomatis via @PreUpdate

        InsurancePlan updatedPlan = insurancePlanRepository.save(plan);
        return convertToResponseDTO(updatedPlan);
    }

    @Override
    public InsurancePlanResponseDTO softDeletePlan(String id) {
        InsurancePlan plan = insurancePlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElse(null);
        if (plan == null) {
            return null;
        }

        if (plan.getOrderedPlans() != null) {
            boolean allExpired = plan.getOrderedPlans().stream()
                    .allMatch(op -> op.getExpiredDate().isBefore(LocalDate.now()));

            if (!allExpired) {
                throw new IllegalStateException("Plan tidak dapat dihapus karena satu atau lebih Ordered Plan terkait belum expired.");
            }
        }
        // Repository menggunakan @SQLDelete, jadi delete() akan memicu soft delete
        insurancePlanRepository.delete(plan);

        return convertToResponseDTO(plan);
    }

    @Override
    public List<InsurancePlanResponseDTO> searchPlansByName(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAllPlans();
        }
        return insurancePlanRepository
                .findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase(keyword)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InsurancePlanResponseDTO> getPlansByProviderId(String providerId) {
        return insurancePlanRepository.findAllByProviderIdAndDeletedAtIsNull(providerId)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<InsurancePlanResponseDTO> getPlansByApplicableService(ServiceEnum service) {
        return insurancePlanRepository
                .findByApplicableServiceContainingAndDeletedAtIsNull(service)
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

   
    // --- HELPER METHODS ---

    private InsurancePlanResponseDTO convertToResponseDTO(InsurancePlan plan) {
        return InsurancePlanResponseDTO.builder()
                .id(plan.getId())
                .providerId(plan.getProviderId())
                .planName(plan.getPlanName())
                .price(plan.getPrice())
                .coverage(plan.getCoverage())
                .coverageDetails(plan.getCoverageDetails())
                .applicableService(plan.getApplicableService())
                .expiredByDays(plan.getExpiredByDays())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }


}