package apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod;

import lombok.Data;

/**
 * Data Transfer Object (DTO) untuk menambahkan metode pembayaran baru.
 * Digunakan oleh admin untuk mendaftarkan opsi pembayaran ke dalam sistem.
 */
@Data
public class AddPaymentMethodRequestDTO {

    /**
     * Nama metode pembayaran (contoh: "QRIS", "Virtual Account").
     */
    private String methodName;

    /**
     * Nama penyedia layanan pembayaran atau bank (contoh: "BCA", "GoPay").
     */
    private String provider;

    /**
     * Status awal metode pembayaran saat dibuat (contoh: "Active").
     */
    private String status;
}