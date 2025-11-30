package apap.ti._5.Insurance_2306275310_be.restdto.response.claim;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO untuk menampilkan detail lengkap satu klaim.
 * Biasanya digunakan pada halaman "Detail Klaim".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDetailResponseDTO {
    
    /** ID unik klaim. */
    private String id;
    
    /** Status terkini (APPROVED, REJECTED, PENDING). */
    private String status;
    
    /** Bukti klaim (teks atau link). */
    private String proof;

    /** Alasan penolakan (jika status REJECTED). */
    private String rejectionReason;

    /** Deskripsi detail penolakan (jika status REJECTED). */
    private String rejectionDescription;

    /** Waktu penolakan dilakukan. */
    private LocalDateTime rejectionTimestamp;

    /** Catatan persetujuan (jika status APPROVED). */
    private String acceptedNote;

    /** Waktu persetujuan dilakukan. */
    private LocalDateTime acceptedTimestamp;
}