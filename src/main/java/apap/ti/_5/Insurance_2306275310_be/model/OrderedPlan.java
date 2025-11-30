package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Merepresentasikan sebuah paket asuransi yang telah dipesan/dibeli (Ordered Plan).
 * Entitas ini menghubungkan antara Polis (Policy) pelanggan dengan Produk Asuransi (InsurancePlan)
 * yang dipilih, serta melacak masa aktif dan klaim yang diajukan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ordered_plan")
public class OrderedPlan {

    /**
     * Identifier unik (Primary Key) untuk pesanan paket ini.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * Status pesanan saat ini (misal: ACTIVE, INACTIVE, CANCELLED).
     */
    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Tanggal berakhirnya masa aktif paket asuransi ini.
     */
    @NotNull
    @Column(name = "expired_date", nullable = false)
    private LocalDate expiredDate;

    /**
     * Referensi ke produk asuransi (InsurancePlan) yang diambil.
     * Menggunakan FetchType.EAGER agar detail produk langsung tersedia saat pesanan diambil.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insurance_plan_id", referencedColumnName = "id")
    @ToString.Exclude
    private InsurancePlan insurancePlan;

    /**
     * Referensi ke Polis (Policy) pemilik pesanan ini.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", referencedColumnName = "id")
    @ToString.Exclude
    private Policy policy;

    /**
     * Daftar klaim (Claim) yang diajukan berdasarkan pesanan paket ini.
     */
    @OneToMany(mappedBy = "orderedPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<Claim> claims;

    /**
     * Waktu data pesanan ini dibuat.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Waktu data pesanan ini terakhir diperbarui.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Method callback sebelum data disimpan (persist).
     * Menginisialisasi createdAt dan updatedAt.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Method callback sebelum data diperbarui (update).
     * Memperbarui updatedAt.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}