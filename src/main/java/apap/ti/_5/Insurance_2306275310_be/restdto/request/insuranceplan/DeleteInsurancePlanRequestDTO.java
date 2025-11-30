package apap.ti._5.Insurance_2306275310_be.restdto.request.insuranceplan;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) untuk permintaan penghapusan paket asuransi.
 * Digunakan untuk mengidentifikasi paket mana yang akan dihapus (Soft Delete).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteInsurancePlanRequestDTO {

    /**
     * ID unik dari paket asuransi yang akan dihapus.
     */
    @NotBlank(message = "Insurance Plan ID must not be empty")
    private String id;
}