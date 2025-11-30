package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.PaymentMethod;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.AddPaymentMethodRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.paymentmethod.UpdatePaymentMethodStatusRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface layanan untuk mengelola Metode Pembayaran.
 */
public interface PaymentMethodService {

    /**
     * Menambahkan metode pembayaran baru.
     *
     * @param request Data metode pembayaran baru.
     * @return Metode pembayaran yang disimpan.
     */
    PaymentMethod addPaymentMethod(AddPaymentMethodRequestDTO request);

    /**
     * Mengambil semua metode pembayaran yang tersedia.
     *
     * @return Daftar metode pembayaran.
     */
    List<PaymentMethod> getAllPaymentMethods();

    /**
     * Memperbarui status (Active/Inactive) metode pembayaran.
     *
     * @param id      ID metode pembayaran.
     * @param request Status baru.
     * @return Metode pembayaran yang diperbarui.
     */
    PaymentMethod updateStatusPaymentMethod(UUID id, UpdatePaymentMethodStatusRequestDTO request);

    /**
     * Menghapus metode pembayaran (Soft Delete).
     *
     * @param id ID metode pembayaran.
     */
    void deletePaymentMethod(UUID id);
}