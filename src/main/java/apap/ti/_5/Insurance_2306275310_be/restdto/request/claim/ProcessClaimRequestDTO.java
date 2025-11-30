package apap.ti._5.Insurance_2306275310_be.restdto.request.claim;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) untuk memproses keputusan klaim.
 * Digunakan saat admin atau sistem menentukan apakah klaim diterima atau ditolak.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessClaimRequestDTO {

    /**
     * Penentu keputusan akhir klaim.
     * True jika klaim disetujui, False jika ditolak.
     */
    @NotNull(message = "Decision (isAccepted) must not be empty")
    private Boolean isAccepted;

    /**
     * Catatan tambahan jika klaim disetujui (Opsional).
     * Hanya relevan jika isAccepted bernilai true.
     */
    private String acceptedNote;

    /**
     * Alasan singkat penolakan klaim (Opsional/Wajib tergantung aturan bisnis).
     * Hanya relevan jika isAccepted bernilai false.
     */
    private String rejectionReason;

    /**
     * Deskripsi detail mengenai alasan penolakan.
     * Hanya relevan jika isAccepted bernilai false.
     */
    private String rejectionDescription;
}