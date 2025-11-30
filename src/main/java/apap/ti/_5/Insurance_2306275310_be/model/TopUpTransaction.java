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
 * Merepresentasikan data transaksi Top-Up saldo pengguna.
 * Mencatat detail jumlah, metode pembayaran, dan status transaksi.
 * Entitas ini mendukung mekanisme Soft Delete menggunakan flag boolean 'is_deleted'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "top_up_transaction")
@Where(clause = "is_deleted = false") // Filter otomatis Hibernate untuk mengabaikan data yang sudah ditandai terhapus
public class TopUpTransaction {

    /**
     * Identifier unik (Primary Key) transaksi top-up.
     * Menggunakan UUID yang digenerate otomatis.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * ID dari pengguna (End User) yang melakukan top-up.
     */
    @Column(name = "end_user_id", nullable = false)
    private UUID endUserId;

    /**
     * Nominal uang yang di-top-up.
     */
    @Column(name = "amount", nullable = false)
    private Long amount;

    /**
     * Status keberhasilan transaksi top-up (contoh: PENDING, SUCCESS, FAILED).
     */
    @Column(name = "status", nullable = false)
    private String status;

    /**
     * Metode pembayaran yang dipilih oleh pengguna untuk transaksi ini.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", nullable = false)
    private PaymentMethod paymentMethod;

    /**
     * Penanda status penghapusan data (Soft Delete).
     * Jika bernilai true, data dianggap tidak aktif/terhapus secara logika.
     */
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    /**
     * Waktu pencatatan transaksi (dibuat otomatis).
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Waktu pembaruan terakhir data transaksi (diupdate otomatis).
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}