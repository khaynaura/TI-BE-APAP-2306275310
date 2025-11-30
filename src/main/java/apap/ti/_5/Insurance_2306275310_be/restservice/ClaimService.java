package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.CreateClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.claim.ProcessClaimRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimDetailResponseDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.response.claim.ClaimSummaryResponseDTO;

import java.util.List;

/**
 * Interface layanan untuk mengelola Klaim asuransi.
 * Menyediakan kontrak untuk operasi CRUD dan logika bisnis terkait klaim.
 */
public interface ClaimService {

    /**
     * Mengambil semua klaim dengan filter opsional berdasarkan status dan ID paket asuransi.
     * @param status Status klaim yang ingin dicari (misal: "WAITING_FOR_REVIEW").
     * @param insurancePlanId ID paket asuransi spesifik.
     * @return Daftar ringkasan klaim yang sesuai filter.
     */
    List<ClaimSummaryResponseDTO> getAllClaimsFiltered(String status, String insurancePlanId);

    /**
     * Mengambil detail lengkap satu klaim berdasarkan ID.
     * @param id ID unik klaim.
     * @return Detail klaim.
     */
    ClaimDetailResponseDTO getClaimById(String id);

    /**
     * Membuat pengajuan klaim baru untuk sebuah paket yang sudah dibeli (OrderedPlan).
     * @param orderedPlanId ID paket yang akan diklaim.
     * @param createDTO Data bukti dan informasi klaim.
     * @return Detail klaim yang baru dibuat.
     */
    ClaimDetailResponseDTO createClaim(String orderedPlanId, CreateClaimRequestDTO createDTO);

    /**
     * Memproses keputusan klaim (Terima/Tolak) oleh admin.
     * @param claimId ID klaim yang akan diproses.
     * @param processDTO Data keputusan (terima/tolak) dan alasannya.
     * @return Detail klaim setelah diproses.
     */
    ClaimDetailResponseDTO processClaim(String claimId, ProcessClaimRequestDTO processDTO);

    /**
     * Memeriksa apakah user tertentu adalah pemilik sah dari klaim tersebut.
     * Digunakan untuk validasi keamanan (Authorization).
     * @param claimId ID klaim.
     * @param userId ID user yang sedang login.
     * @return True jika user adalah pemilik, False jika bukan.
     */
    boolean isClaimOwner(String claimId, String userId);
}