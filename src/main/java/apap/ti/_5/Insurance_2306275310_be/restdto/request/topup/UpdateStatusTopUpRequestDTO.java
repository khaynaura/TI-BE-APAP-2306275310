package apap.ti._5.Insurance_2306275310_be.restdto.request.topup;

import lombok.Data;

/**
 * Data Transfer Object (DTO) untuk memperbarui status transaksi Top-Up.
 * Biasanya digunakan oleh sistem pembayaran (callback) atau admin.
 */
@Data
public class UpdateStatusTopUpRequestDTO {

    /**
     * Status baru transaksi (contoh: "SUCCESS", "FAILED").
     */
    private String status;
}