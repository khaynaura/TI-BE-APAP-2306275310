package apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod;

import lombok.Data;

/**
 * Data Transfer Object (DTO) khusus untuk memperbarui status metode pembayaran.
 * Digunakan untuk mengaktifkan atau menonaktifkan metode pembayaran tanpa mengubah detail lainnya.
 */
@Data
public class UpdatePaymentMethodStatusRequestDTO {

    /**
     * Status baru yang ingin diterapkan (contoh: "Active" atau "Inactive").
     */
    private String status;
}