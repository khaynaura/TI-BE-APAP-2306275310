package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Merepresentasikan sebuah Polis Asuransi (Policy).
 * Entitas ini adalah kontrak induk yang mengikat User dengan satu atau lebih
 * paket asuransi (OrderedPlan) yang dibeli dalam satu transaksi booking.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "policy")
public class Policy {

    /**
     * Identifier unik (Primary Key) untuk polis ini.
     */
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    /**
     * ID pemesanan yang terkait dengan polis ini (misal: ID Booking Tiket/Hotel).
     */
    @NotNull
    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    /**
     * ID pengguna (Customer) pemilik polis ini.
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private String userId;

    /**
     * ID Tagihan (Bill) yang terkait dengan pembayaran polis ini.
     */
    @Column(name = "bill_id")
    private String billId;

    /**
     * Tanggal mulai berlakunya polis.
     */
    @NotNull
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * Status terkini dari polis (misal: ACTIVE, CANCELLED, WAITING_PAYMENT).
     */
    @NotNull
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Jenis layanan utama yang diasuransikan (misal: FLIGHT, HOTEL).
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service", nullable = false)
    private ServiceEnum service;

    /**
     * Total nilai tanggungan gabungan dari semua paket dalam polis ini.
     */
    @NotNull
    @Column(name = "total_coverage", nullable = false)
    private Integer totalCoverage;

    /**
     * Total harga premi yang harus dibayar user.
     */
    @NotNull
    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    /**
     * Daftar paket asuransi spesifik (OrderedPlan) yang termasuk dalam polis ini.
     */
    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<OrderedPlan> orderedPlans;

    /**
     * Waktu data polis ini dibuat.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Waktu data polis ini terakhir diperbarui.
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