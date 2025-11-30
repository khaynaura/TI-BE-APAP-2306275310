package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Merepresentasikan metode pembayaran yang tersedia dalam sistem.
 * Entitas ini mencatat detail provider pembayaran dan status aktifnya.
 * Menggunakan mekanisme Soft Delete berbasis boolean (is_deleted).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_method")
@Where(clause = "is_deleted = false") // Filter otomatis untuk mengabaikan data yang sudah dihapus secara logical
public class PaymentMethod {

    /**
     * Identifier unik (Primary Key) untuk metode pembayaran.
     * Menggunakan tipe data UUID yang digenerate secara otomatis.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Nama metode pembayaran (contoh: "Kartu Kredit", "Transfer Bank").
     */
    @Column(name = "method_name", nullable = false)
    private String methodName;

    /**
     * Nama penyedia layanan pembayaran (contoh: "Visa", "BCA", "GoPay").
     */
    @Column(name = "provider", nullable = false)
    private String provider;

    /**
     * Status operasional metode pembayaran (contoh: "Active", "Inactive").
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Penanda apakah metode pembayaran ini sudah dihapus (Soft Delete).
     * Jika true, data dianggap tidak ada tapi tetap tersimpan di database.
     */
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    /**
     * Waktu data metode pembayaran ini dibuat.
     * Diisi otomatis oleh Hibernate saat insert.
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Waktu data metode pembayaran ini terakhir diperbarui.
     * Diisi otomatis oleh Hibernate saat update.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}