package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.response.orderedplan.OrderedPlanDetailResponseDTO;

/**
 * Interface layanan untuk melihat detail Ordered Plan (Item Asuransi yang dibeli).
 */
public interface OrderedPlanService {

    /**
     * Mengambil detail Ordered Plan beserta riwayat klaimnya.
     *
     * @param orderedPlanId ID Ordered Plan.
     * @return Detail Ordered Plan.
     */
    OrderedPlanDetailResponseDTO getOrderedPlanDetailById(String orderedPlanId);
}