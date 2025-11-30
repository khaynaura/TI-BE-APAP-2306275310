package apap.ti._5.Insurance_2306275310_be.restdto.request.claim;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) untuk menangani permintaan pembuatan klaim baru.
 * Berisi data yang wajib dikirimkan client saat mengajukan klaim.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateClaimRequestDTO {

    /**
     * Bukti pendukung klaim yang diajukan.
     * Field ini wajib diisi, bisa berupa deskripsi teks atau URL file bukti.
     */
    @NotBlank(message = "Proof must not be empty")
    private String proof;
}