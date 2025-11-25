package apap.ti._5.Insurance_2306275310_be.repository;

import apap.ti._5.Insurance_2306275310_be.model.OrderedPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime; 
import java.util.List;         

@Repository
public interface OrderedPlanRepository extends JpaRepository<OrderedPlan, String> {
    
    @Query(value = "SELECT EXTRACT(MONTH FROM created_at) as month, COUNT(id) as count " +
    "FROM ordered_plan " +
    "WHERE created_at >= :startDate " +
    "GROUP BY EXTRACT(MONTH FROM created_at) " + 
    "ORDER BY month ASC", nativeQuery = true)
List<MonthlyOrderCount> findMonthlyOrderCounts(@Param("startDate") LocalDateTime startDate);

@Query(value = "SELECT EXTRACT(MONTH FROM op.created_at) as month, COUNT(op.id) as count " +
    "FROM ordered_plan op JOIN policy p ON op.policy_id = p.id " +
    "WHERE op.created_at >= :startDate AND p.service = :service " +
    "GROUP BY EXTRACT(MONTH FROM op.created_at) " + 
    "ORDER BY month ASC", nativeQuery = true)
List<MonthlyOrderCount> findMonthlyOrderCountsByService(@Param("startDate") LocalDateTime startDate, @Param("service") String service);

@Query(value = """
    SELECT EXTRACT(MONTH FROM op.created_at) as month, COUNT(op.id) as count
    FROM ordered_plan op
    JOIN insurance_plan ip ON op.insurance_plan_id = ip.id
    WHERE op.created_at >= :startDate
    AND (:service IS NULL OR ip.service LIKE CONCAT('%', :service, '%'))
    AND (:providerId IS NULL OR ip.provider_id = :providerId)
    GROUP BY EXTRACT(MONTH FROM op.created_at)
    ORDER BY month ASC
    """, nativeQuery = true)
List<MonthlyOrderCount> findMonthlyStats(
    @Param("startDate") LocalDateTime startDate, 
    @Param("service") String service,
    @Param("providerId") String providerId
);

}