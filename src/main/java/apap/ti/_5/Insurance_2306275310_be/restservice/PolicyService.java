package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.request.policy.CreatePolicyRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.policy.PolicyResponseDTO;

import java.util.List;

/**
 * Interface layanan untuk entitas Policy.
 * Mendefinisikan kontrak operasi bisnis terkait pembelian dan manajemen polis asuransi.
 */
public interface PolicyService {

    /**
     * Membuat polis baru.
     *
     * @param createDTO Data permintaan pembuatan polis.
     * @return DTO polis yang berhasil dibuat.
     */
    PolicyResponseDTO createPolicy(CreatePolicyRequestDTO createDTO);

    /**
     * Mengambil semua polis yang ada di sistem (biasanya untuk Admin).
     *
     * @return Daftar semua polis.
     */
    List<PolicyResponseDTO> getAllPolicies();

    /**
     * Mengambil detail polis berdasarkan ID-nya.
     *
     * @param policyId ID polis.
     * @return Detail polis.
     */
    PolicyResponseDTO getPolicyById(String policyId);

    /**
     * Memproses pembayaran polis (Callback dari Billing Service).
     *
     * @param policyId ID polis yang dibayar.
     * @return Data polis yang statusnya sudah diperbarui menjadi PAID.
     */
    PolicyResponseDTO payPolicy(String policyId);

    /**
     * Mengambil daftar polis milik user tertentu (Customer).
     *
     * @param userId ID user (customer).
     * @return Daftar polis milik user tersebut.
     */
    List<PolicyResponseDTO> getPoliciesByUserId(String userId);
}