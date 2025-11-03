package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, String> {

    // filter by status
    List<Claim> findAllByStatus(String status);

    // filter by Insurance Plan ID (from OrderedPlan)
    List<Claim> findAllByOrderedPlan_InsurancePlan_Id(String insurancePlanId);

    // filter by Status and Insurance Plan ID
    List<Claim> findAllByStatusAndOrderedPlan_InsurancePlan_Id(String status, String insurancePlanId);
}