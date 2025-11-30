package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository untuk mengakses data {@link Claim} dari database.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    /**
     * Mengambil semua klaim dan mengurutkannya berdasarkan waktu pembuatan (terbaru).
     *
     * @return Daftar semua klaim.
     */
    List<Claim> findAllByOrderByCreatedAtDesc();

    /**
     * Mengambil klaim berdasarkan status tertentu.
     *
     * @param status Status klaim (misal: WAITING_FOR_REVIEW, ACCEPTED).
     * @return Daftar klaim sesuai status.
     */
    List<Claim> findAllByStatusOrderByCreatedAtDesc(String status);

    /**
     * Mengambil klaim berdasarkan ID Insurance Plan yang terkait dengan Ordered Plan-nya.
     *
     * @param insurancePlanId ID Insurance Plan.
     * @return Daftar klaim terkait plan tersebut.
     */
    List<Claim> findAllByOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(String insurancePlanId);

    /**
     * Mengambil klaim berdasarkan kombinasi Status dan ID Insurance Plan.
     *
     * @param status          Status klaim.
     * @param insurancePlanId ID Insurance Plan.
     * @return Daftar klaim yang cocok.
     */
    List<Claim> findAllByStatusAndOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(String status, String insurancePlanId);

    /**
     * Menghitung jumlah klaim yang diajukan oleh Customer tertentu.
     *
     * @param userId ID Customer.
     * @return Jumlah klaim.
     */
    @Query("SELECT COUNT(c) FROM Claim c JOIN c.orderedPlan op JOIN op.policy p WHERE p.userId = :userId")
    long countByCustomerUserId(@Param("userId") String userId);

    /**
     * Menghitung jumlah klaim yang masuk untuk Provider tertentu.
     *
     * @param providerId ID Provider.
     * @return Jumlah klaim.
     */
    @Query("SELECT COUNT(c) FROM Claim c JOIN c.orderedPlan op JOIN op.insurancePlan ip WHERE ip.providerId = :providerId")
    long countByProviderId(@Param("providerId") String providerId);
}