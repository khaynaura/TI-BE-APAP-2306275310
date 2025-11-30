package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository untuk mengakses data {@link InsurancePlan}.
 * Mengimplementasikan filter otomatis untuk Soft Delete (deleted_at IS NULL).
 */
@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, String> {

    /**
     * Mengambil semua Insurance Plan yang tidak terhapus (Soft Delete).
     */
    List<InsurancePlan> findAllByDeletedAtIsNull();

    /**
     * Mengambil Insurance Plan berdasarkan ID jika belum dihapus.
     */
    Optional<InsurancePlan> findByIdAndDeletedAtIsNull(String id);

    /**
     * Mencari Insurance Plan berdasarkan Service yang didukung (misal: FLIGHT, ACCOMMODATION).
     */
    List<InsurancePlan> findByApplicableServiceContainingAndDeletedAtIsNull(ServiceEnum service);

    /**
     * Mencari Insurance Plan berdasarkan nama plan (Case Insensitive).
     */
    List<InsurancePlan> findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase(String planName);

    /**
     * Menghitung total semua plan, termasuk yang sudah di-soft delete.
     * Menggunakan Native Query untuk mengabaikan filter Hibernate @Where.
     */
    @Query(value = "SELECT count(*) FROM insurance_plan", nativeQuery = true)
    long countAll();

    /**
     * Menghitung jumlah plan yang aktif (belum dihapus).
     */
    long countByDeletedAtIsNull();

    /**
     * Mengambil semua plan milik Provider tertentu yang belum dihapus.
     */
    List<InsurancePlan> findAllByProviderIdAndDeletedAtIsNull(String providerId);

    /**
     * Menghitung jumlah plan milik Provider tertentu yang belum dihapus.
     */
    long countByProviderIdAndDeletedAtIsNull(String providerId);
}