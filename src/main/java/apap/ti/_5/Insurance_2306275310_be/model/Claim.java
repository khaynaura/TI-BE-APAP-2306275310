package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "claim")
public class Claim {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @NotNull
    @Column(name = "status", nullable = false)
    private String status; 

    @NotNull
    @Lob
    @Column(name = "proof", nullable = false, columnDefinition = "TEXT")
    private String proof;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Lob
    @Column(name = "rejection_description", columnDefinition = "TEXT")
    private String rejectionDescription;

    @Column(name = "rejection_timestamp")
    private LocalDateTime rejectionTimestamp;

    @Lob
    @Column(name = "accepted_note", columnDefinition = "TEXT")
    private String acceptedNote;

    @Column(name = "accepted_timestamp")
    private LocalDateTime acceptedTimestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_plan_id", referencedColumnName = "id")
    @ToString.Exclude
    private OrderedPlan orderedPlan;

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