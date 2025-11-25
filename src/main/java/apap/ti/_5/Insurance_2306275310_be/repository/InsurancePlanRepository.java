package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.InsurancePlan;
import apap.ti._5.Insurance_2306275310_be.model.ServiceEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsurancePlanRepository extends JpaRepository<InsurancePlan, String> {

    // all plan yang 'deleted_at' nya NULL (tidak terhapus)
    List<InsurancePlan> findAllByDeletedAtIsNull();

    // plan with ID dan 'deleted_at' nya NULL
    Optional<InsurancePlan> findByIdAndDeletedAtIsNull(String id);

    // plan berdasarkan service dan 'deleted_at' nya NULL
    // for create policy
    List<InsurancePlan> findByApplicableServiceContainingAndDeletedAtIsNull(ServiceEnum service);

    List<InsurancePlan> findAllByDeletedAtIsNullAndPlanNameContainingIgnoreCase(String planName);
    
    // count all plan, including yg di soft delete
    @Query(value = "SELECT count(*) FROM insurance_plan", nativeQuery = true)
    long countAll();

    long countByDeletedAtIsNull();

    List<InsurancePlan> findAllByProviderIdAndDeletedAtIsNull(String providerId);

}