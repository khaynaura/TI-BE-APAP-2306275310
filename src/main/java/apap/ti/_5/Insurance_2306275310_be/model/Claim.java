package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Merepresentasikan entitas Klaim (Claim) dalam sistem asuransi.
 * Class ini menyimpan seluruh informasi terkait pengajuan klaim,
 * termasuk status persetujuan, bukti, dan riwayat waktu.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "claim")
public class Claim {

    /**
     * Identifier unik (Primary Key) untuk setiap pengajuan klaim.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * Status terkini dari pengajuan klaim (misal: PENDING, APPROVED, REJECTED).
     */
    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Bukti pendukung klaim (misal: deskripsi atau link file), disimpan sebagai teks panjang.
     */
    @NotNull
    @Lob
    @Column(name = "proof", nullable = false, columnDefinition = "TEXT")
    private String proof;

    /**
     * Alasan singkat mengapa klaim ditolak (jika status REJECTED).
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    /**
     * Penjelasan mendetail mengenai penolakan klaim.
     */
    @Lob
    @Column(name = "rejection_description", columnDefinition = "TEXT")
    private String rejectionDescription;

    /**
     * Waktu ketika klaim ditolak.
     */
    @Column(name = "rejection_timestamp")
    private LocalDateTime rejectionTimestamp;

    /**
     * Catatan tambahan yang diberikan saat klaim diterima/disetujui.
     */
    @Lob
    @Column(name = "accepted_note", columnDefinition = "TEXT")
    private String acceptedNote;

    /**
     * Waktu ketika klaim diterima/disetujui.
     */
    @Column(name = "accepted_timestamp")
    private LocalDateTime acceptedTimestamp;

    /**
     * Referensi ke entitas OrderedPlan yang terkait dengan klaim ini.
     * Menggunakan FetchType.LAZY untuk performa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordered_plan_id", referencedColumnName = "id")
    @ToString.Exclude
    private OrderedPlan orderedPlan;

    /**
     * Waktu data klaim ini pertama kali dibuat di database.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Waktu data klaim ini terakhir kali diperbarui.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Method callback yang dijalankan otomatis sebelum data disimpan pertama kali (persist).
     * Mengatur nilai createdAt dan updatedAt ke waktu sekarang.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Method callback yang dijalankan otomatis sebelum data diperbarui (update).
     * Memperbarui nilai updatedAt ke waktu sekarang.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}