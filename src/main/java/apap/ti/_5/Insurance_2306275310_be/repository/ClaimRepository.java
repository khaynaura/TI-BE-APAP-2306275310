package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    // 1. Ambil semua, urutkan dari yang terbaru
    List<Claim> findAllByOrderByCreatedAtDesc();

    // 2. Filter by status, urutkan dari yang terbaru
    List<Claim> findAllByStatusOrderByCreatedAtDesc(String status);

    // 3. Filter by Insurance Plan ID, urutkan dari yang terbaru
    List<Claim> findAllByOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(String insurancePlanId);

    // 4. Filter by Status AND Insurance Plan ID, urutkan dari yang terbaru
    List<Claim> findAllByStatusAndOrderedPlan_InsurancePlan_IdOrderByCreatedAtDesc(String status, String insurancePlanId);

    // Untuk Customer: Hitung claim yang dia ajukan
    @Query("SELECT COUNT(c) FROM Claim c JOIN c.orderedPlan op JOIN op.policy p WHERE p.userId = :userId")
    long countByCustomerUserId(@Param("userId") String userId);

    // Untuk Provider: Hitung claim yang masuk ke dia
    @Query("SELECT COUNT(c) FROM Claim c JOIN c.orderedPlan op JOIN op.insurancePlan ip WHERE ip.providerId = :providerId")
    long countByProviderId(@Param("providerId") String providerId);
}