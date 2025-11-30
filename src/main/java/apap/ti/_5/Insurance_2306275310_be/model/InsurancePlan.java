package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Merepresentasikan sebuah paket/rencana asuransi yang ditawarkan oleh penyedia (provider).
 * Class ini mendukung mekanisme "Soft Delete" dimana data tidak dihapus permanen,
 * melainkan hanya ditandai dengan timestamp pada kolom deleted_at.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "insurance_plan")
@SQLDelete(sql = "UPDATE insurance_plan SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class InsurancePlan {

    /**
     * Identifier unik (Primary Key) untuk paket asuransi.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * ID dari penyedia asuransi (Provider) yang menerbitkan paket ini.
     */
    @NotNull
    @Column(name = "provider_id", nullable = false)
    private String providerId;

    /**
     * Nama paket asuransi.
     */
    @NotNull
    @Column(name = "plan_name", nullable = false)
    private String planName;

    /**
     * Harga premi asuransi.
     */
    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

    /**
     * Nilai tanggungan (coverage) maksimal yang diberikan oleh paket ini.
     */
    @NotNull
    @Column(name = "coverage", nullable = false)
    private Integer coverage;

    /**
     * Detail penjelasan mengenai cakupan asuransi dalam format teks panjang.
     */
    @NotNull
    @Lob
    @Column(name = "coverage_details", nullable = false, columnDefinition = "TEXT")
    private String coverageDetails;

    /**
     * Daftar jenis layanan (ServiceEnum) yang termasuk dalam paket ini.
     * Disimpan dalam tabel terpisah 'applicable_services'.
     */
    @NotNull
    @ElementCollection(targetClass = ServiceEnum.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "applicable_services", joinColumns = @JoinColumn(name = "insurance_plan_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service", nullable = false)
    private List<ServiceEnum> applicableService;

    /**
     * Masa berlaku asuransi dalam satuan hari.
     */
    @NotNull
    @Column(name = "expired_by_days", nullable = false)
    private Integer expiredByDays;

    /**
     * Daftar pesanan (OrderedPlan) yang mengambil paket asuransi ini.
     */
    @OneToMany(mappedBy = "insurancePlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderedPlan> orderedPlans;

    /**
     * Waktu data paket ini dibuat.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Waktu data paket ini terakhir diperbarui.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Waktu data paket ini dihapus (Soft Delete).
     * Jika null, berarti data masih aktif.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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