package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository untuk mengakses data {@link Policy}.
 */
@Repository
public interface PolicyRepository extends JpaRepository<Policy, String> {

    /**
     * Mengambil daftar polis milik user tertentu.
     *
     * @param userId ID User.
     * @return List Policy.
     */
    List<Policy> findAllByUserId(String userId);

    /**
     * Menghitung jumlah polis yang dimiliki user tertentu (untuk Dashboard Customer).
     *
     * @param userId ID User.
     * @return Jumlah polis.
     */
    long countByUserId(String userId);

    /**
     * Menghitung jumlah OrderedPlan (Polis terjual) milik Provider tertentu.
     * Digunakan untuk Dashboard Provider.
     *
     * @param providerId ID Provider.
     * @return Jumlah plan terjual.
     */
    @Query("SELECT COUNT(op) FROM OrderedPlan op JOIN op.insurancePlan ip WHERE ip.providerId = :providerId")
    long countByProviderId(@Param("providerId") String providerId);
}