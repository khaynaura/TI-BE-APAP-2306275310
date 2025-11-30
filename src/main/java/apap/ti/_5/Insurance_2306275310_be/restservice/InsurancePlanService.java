package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.CreateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan.UpdateInsurancePlanRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.insuranceplan.InsurancePlanResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.ProviderDTO;

import java.util.List;

/**
 * Interface layanan untuk manajemen Insurance Plan (Produk Asuransi).
 */
public interface InsurancePlanService {

    /**
     * Membuat plan asuransi baru.
     *
     * @param createDTO Data plan baru.
     * @return Plan yang berhasil dibuat.
     */
    InsurancePlanResponseDTO createInsurancePlan(CreateInsurancePlanRequestDTO createDTO);

    /**
     * Mengambil semua plan yang tersedia (belum dihapus).
     *
     * @return Daftar semua plan.
     */
    List<InsurancePlanResponseDTO> getAllPlans();

    /**
     * Mengambil detail plan berdasarkan ID.
     *
     * @param id ID plan.
     * @return Detail plan.
     */
    InsurancePlanResponseDTO getPlanById(String id);

    /**
     * Memperbarui data plan asuransi.
     *
     * @param updateDTO Data perubahan.
     * @return Plan yang sudah diperbarui.
     */
    InsurancePlanResponseDTO updateInsurancePlan(UpdateInsurancePlanRequestDTO updateDTO);

    /**
     * Menghapus plan secara soft-delete.
     *
     * @param id ID plan yang akan dihapus.
     * @return Data plan yang dihapus.
     */
    InsurancePlanResponseDTO softDeletePlan(String id);

    /**
     * Mencari plan berdasarkan kata kunci nama.
     *
     * @param keyword Kata kunci pencarian.
     * @return Daftar plan yang cocok.
     */
    List<InsurancePlanResponseDTO> searchPlansByName(String keyword);

    /**
     * Memfilter plan berdasarkan jenis layanan (ServiceEnum).
     *
     * @param service Jenis layanan (Accommodation, Flight, dll).
     * @return Daftar plan yang sesuai.
     */
    List<InsurancePlanResponseDTO> getPlansByApplicableService(ServiceEnum service);

    /**
     * Memfilter plan berdasarkan ID Provider pembuatnya.
     *
     * @param providerId ID Provider.
     * @return Daftar plan milik provider tersebut.
     */
    List<InsurancePlanResponseDTO> getPlansByProviderId(String providerId);

}