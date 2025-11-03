package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_plan")
@SQLDelete(sql = "UPDATE insurance_plan SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class InsurancePlan {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id; 

    @NotNull
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @NotNull
    @Column(name = "plan_name", nullable = false)
    private String planName; 

    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

    @NotNull
    @Column(name = "coverage", nullable = false)
    private Integer coverage;

    @NotNull
    @Lob
    @Column(name = "coverage_details", nullable = false, columnDefinition = "TEXT")
    private String coverageDetails;

    @NotNull
    @ElementCollection(targetClass = ServiceEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "applicable_services", joinColumns = @JoinColumn(name = "insurance_plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service", nullable = false)
    private List<ServiceEnum> applicableService; 

    @NotNull
    @Column(name = "expired_by_days", nullable = false)
    private Integer expiredByDays; 

    @OneToMany(mappedBy = "insurancePlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude 
    private List<OrderedPlan> orderedPlans; 

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}