package apap.ti._5.Insurance_2306275310_be.restservice;

import apap.ti._5.Insurance_2306275310_be.model.TopUpTransaction;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.CreateTopUpRequestDTO;
import apap.ti._5.Insurance_2306275310_be.restdto.request.topup.UpdateStatusTopUpRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Interface layanan untuk transaksi Top-Up Saldo.
 */
public interface TopUpService {

    /**
     * Membuat transaksi top-up baru (Pending).
     *
     * @param request Data request top-up (jumlah, metode pembayaran).
     * @return Transaksi yang baru dibuat.
     */
    TopUpTransaction createTopUp(CreateTopUpRequestDTO request);

    /**
     * Memperbarui status top-up (Success/Failed).
     * Jika Success, saldo user di Profile Service akan bertambah.
     *
     * @param transactionId ID Transaksi.
     * @param request       Status baru.
     * @return Transaksi yang diperbarui.
     */
    TopUpTransaction updateStatusTopUp(UUID transactionId, UpdateStatusTopUpRequestDTO request);

    /**
     * Mengambil riwayat top-up milik user tertentu.
     *
     * @param userId ID User.
     * @return Daftar transaksi user.
     */
    List<TopUpTransaction> getHistoryByUserId(UUID userId);

    /**
     * Mengambil semua transaksi top-up (Admin).
     *
     * @return Daftar semua transaksi.
     */
    List<TopUpTransaction> getAllTransactions();

    /**
     * Mengambil detail transaksi berdasarkan ID.
     *
     * @param transactionId ID Transaksi.
     * @return Detail transaksi.
     */
    TopUpTransaction getTransactionById(UUID transactionId);

    /**
     * Menghapus transaksi top-up (Soft Delete).
     *
     * @param transactionId ID Transaksi.
     */
    void deleteTopUpTransaction(UUID transactionId);
}