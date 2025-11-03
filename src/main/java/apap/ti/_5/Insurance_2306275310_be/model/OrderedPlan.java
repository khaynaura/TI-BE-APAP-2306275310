package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordered_plan")
public class OrderedPlan {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status; 

    @NotNull
    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insurance_plan_id", referencedColumnName = "id")
    @ToString.Exclude
    private InsurancePlan insurancePlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "id")
    @ToString.Exclude
    private Policy policy;

    @OneToMany(mappedBy = "orderedPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Claim> claims;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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