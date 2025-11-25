package apap.ti._5.Insurance_2306275310_be.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Where; // <--- JANGAN LUPA IMPORT INI

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "top_up_transaction")
@Where(clause = "is_deleted = false") // <--- WAJIB ADA: Biar data terhapus otomatis hilang
public class TopUpTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "end_user_id", nullable = false)
    private UUID endUserId; 

    @Column(name = "amount", nullable = false)
    private Long amount; 

    @Column(name = "status", nullable = false)
    private String status; 

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_method_id", referencedColumnName = "id", nullable = false)
    private PaymentMethod paymentMethod;

    // --- TAMBAHAN WAJIB DI BAWAH INI ---
    @Column(name = "is_deleted")
    private boolean isDeleted = false;
    // ------------------------------------

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}