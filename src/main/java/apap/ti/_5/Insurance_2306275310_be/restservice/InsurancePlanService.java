package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;

import java.util.List;

public interface InsurancePlanService {

    InsurancePlanResponseDTO createInsurancePlan(CreateInsurancePlanRequestDTO createDTO);

    List<InsurancePlanResponseDTO> getAllPlans();

    InsurancePlanResponseDTO getPlanById(String id);

    InsurancePlanResponseDTO updateInsurancePlan(UpdateInsurancePlanRequestDTO updateDTO);

    InsurancePlanResponseDTO softDeletePlan(String id);

    List<InsurancePlanResponseDTO> searchPlansByName(String keyword);

    List<InsurancePlanResponseDTO> getPlansByApplicableService(ServiceEnum service); // untuk fitur di policy

    List<InsurancePlanResponseDTO> getPlansByProviderId(String providerId);

}