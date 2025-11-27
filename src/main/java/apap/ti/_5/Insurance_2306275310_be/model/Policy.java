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
@Table(name = "policy")
public class Policy {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @NotNull
    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status; 

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service", nullable = false)
    private ServiceEnum service;

    @NotNull
    @Column(name = "total_coverage", nullable = false)
    private Integer totalCoverage;

    @NotNull
    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderedPlan> orderedPlans;

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

    @Column(name = "bill_id") 
    private String billId; 
}