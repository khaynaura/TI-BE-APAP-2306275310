package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;

public interface OrderedPlanService {

    OrderedPlanDetailResponseDTO getOrderedPlanDetailById(String orderedPlanId);
}